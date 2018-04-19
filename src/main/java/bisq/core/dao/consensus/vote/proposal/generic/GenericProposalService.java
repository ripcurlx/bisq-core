/*
 * This file is part of Bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao.consensus.vote.proposal.generic;

import bisq.core.btc.exceptions.TransactionVerificationException;
import bisq.core.btc.exceptions.WalletException;
import bisq.core.btc.wallet.BsqWalletService;
import bisq.core.btc.wallet.BtcWalletService;
import bisq.core.dao.consensus.state.StateService;
import bisq.core.dao.consensus.state.events.payloads.GenericProposalPayload;
import bisq.core.dao.consensus.vote.proposal.Proposal;
import bisq.core.dao.consensus.vote.proposal.ProposalConsensus;
import bisq.core.dao.consensus.vote.proposal.ProposalPayloadValidator;
import bisq.core.dao.consensus.vote.proposal.ValidationException;
import bisq.core.dao.consensus.vote.proposal.param.ParamService;

import bisq.common.crypto.KeyRing;
import bisq.common.util.Tuple2;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;

import javax.inject.Inject;

import java.security.PublicKey;

import java.io.IOException;

import java.util.Date;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenericProposalService {
    private final BsqWalletService bsqWalletService;
    private final BtcWalletService btcWalletService;
    private final ParamService paramService;
    private final ProposalPayloadValidator proposalPayloadValidator;
    private final PublicKey signaturePubKey;
    private final StateService stateService;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public GenericProposalService(BsqWalletService bsqWalletService,
                                  BtcWalletService btcWalletService,
                                  StateService stateService,
                                  ParamService paramService,
                                  ProposalPayloadValidator proposalPayloadValidator,
                                  KeyRing keyRing) {
        this.bsqWalletService = bsqWalletService;
        this.btcWalletService = btcWalletService;
        this.stateService = stateService;
        this.paramService = paramService;
        this.proposalPayloadValidator = proposalPayloadValidator;

        signaturePubKey = keyRing.getPubKeyRing().getSignaturePubKey();
    }

    public Tuple2<Proposal, Transaction> makeTxAndGetGenericProposal(String name,
                                                                     String title,
                                                                     String description,
                                                                     String link)
            throws ValidationException, InsufficientMoneyException, IOException, TransactionVerificationException,
            WalletException {

        // As we don't know the txId we create a temp object with TxId set to null.
        final GenericProposalPayload tempPayload = new GenericProposalPayload(
                UUID.randomUUID().toString(),
                name,
                title,
                description,
                link,
                signaturePubKey,
                new Date()
        );
        proposalPayloadValidator.validateDataFields(tempPayload);

        Transaction transaction = createGenericProposalTx(tempPayload);

        return new Tuple2<>(createGenericProposal(tempPayload, transaction), transaction);
    }

    // We have txId set to null in tempPayload as we cannot know it before the tx is created.
    // Once the tx is known we will create a new object including the txId.
    // The hashOfPayload used in the opReturnData is created with the txId set to null.
    private Transaction createGenericProposalTx(GenericProposalPayload tempPayload)
            throws InsufficientMoneyException, TransactionVerificationException, WalletException, IOException {

        final Coin fee = ProposalConsensus.getFee(paramService, stateService.getChainHeight());
        final Transaction preparedBurnFeeTx = bsqWalletService.getPreparedBurnFeeTx(fee);

        // payload does not have txId at that moment
        byte[] hashOfPayload = ProposalConsensus.getHashOfPayload(tempPayload);
        byte[] opReturnData = ProposalConsensus.getOpReturnData(hashOfPayload);

        final Transaction txWithBtcFee = btcWalletService.completePreparedGenericProposalTx(
                preparedBurnFeeTx,
                opReturnData);

        final Transaction completedTx = bsqWalletService.signTx(txWithBtcFee);
        log.info("GenericProposal tx: " + completedTx);
        return completedTx;
    }

    // We have txId set to null in tempPayload as we cannot know it before the tx is created.
    // Once the tx is known we will create a new object including the txId.
    private GenericProposal createGenericProposal(GenericProposalPayload tempPayload, Transaction transaction) {
        final String txId = transaction.getHashAsString();
        GenericProposalPayload compensationRequestPayload = (GenericProposalPayload) tempPayload.cloneWithTxId(txId);
        return new GenericProposal(compensationRequestPayload);
    }
}
