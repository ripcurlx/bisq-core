/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.trade.protocol.tasks.buyer_as_taker;

import bisq.core.btc.AddressEntry;
import bisq.core.btc.data.RawTransactionInput;
import bisq.core.btc.wallet.BtcWalletService;
import bisq.core.trade.Trade;
import bisq.core.trade.protocol.TradingPeer;
import bisq.core.trade.protocol.tasks.TradeTask;
import com.google.common.util.concurrent.FutureCallback;
import io.bisq.common.crypto.Hash;
import io.bisq.common.taskrunner.TaskRunner;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class BuyerAsTakerSignAndPublishDepositTx extends TradeTask {

    @SuppressWarnings({"WeakerAccess", "unused"})
    public BuyerAsTakerSignAndPublishDepositTx(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();

            log.debug("\n\n------------------------------------------------------------\n"
                    + "Contract as json\n"
                    + trade.getContractAsJson()
                    + "\n------------------------------------------------------------\n");


            byte[] contractHash = Hash.getSha256Hash(trade.getContractAsJson());
            trade.setContractHash(contractHash);
            List<RawTransactionInput> buyerInputs = checkNotNull(processModel.getRawTransactionInputs(), "buyerInputs must not be null");
            BtcWalletService walletService = processModel.getBtcWalletService();
            String id = processModel.getOffer().getId();

            Optional<AddressEntry> addressEntryOptional = walletService.getAddressEntry(id, AddressEntry.Context.MULTI_SIG);
            checkArgument(addressEntryOptional.isPresent(), "addressEntryOptional must be present");
            AddressEntry buyerMultiSigAddressEntry = addressEntryOptional.get();
            Coin buyerInput = Coin.valueOf(buyerInputs.stream().mapToLong(input -> input.value).sum());

            buyerMultiSigAddressEntry.setCoinLockedInMultiSig(buyerInput.subtract(trade.getTxFee().multiply(2)));
            walletService.saveAddressEntryList();

            TradingPeer tradingPeer = processModel.getTradingPeer();
            byte[] buyerMultiSigPubKey = processModel.getMyMultiSigPubKey();
            checkArgument(Arrays.equals(buyerMultiSigPubKey, buyerMultiSigAddressEntry.getPubKey()),
                    "buyerMultiSigPubKey from AddressEntry must match the one from the trade data. trade id =" + id);

            Transaction depositTx = processModel.getTradeWalletService().takerSignsAndPublishesDepositTx(
                    false,
                    contractHash,
                    processModel.getPreparedDepositTx(),
                    buyerInputs,
                    tradingPeer.getRawTransactionInputs(),
                    buyerMultiSigPubKey,
                    tradingPeer.getMultiSigPubKey(),
                    trade.getArbitratorBtcPubKey(),
                    new FutureCallback<Transaction>() {
                        @Override
                        public void onSuccess(Transaction transaction) {
                            if (!completed) {
                                log.trace("takerSignAndPublishTx succeeded " + transaction);
                                trade.setState(Trade.State.TAKER_PUBLISHED_DEPOSIT_TX);
                                walletService.swapTradeEntryToAvailableEntry(id, AddressEntry.Context.RESERVED_FOR_TRADE);

                                complete();
                            } else {
                                log.warn("We got the onSuccess callback called after the timeout has been triggered a complete().");
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Throwable t) {
                            if (!completed) {
                                failed(t);
                            } else {
                                log.warn("We got the onFailure callback called after the timeout has been triggered a complete().");
                            }
                        }
                    });
            // We set the deposit tx in case we get the onFailure called.
            trade.setDepositTx(depositTx);
        } catch (Throwable t) {
            failed(t);
        }
    }
}
