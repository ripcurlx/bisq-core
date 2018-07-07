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

package bisq.core.dao.node.validation;

import bisq.core.dao.state.StateService;
import bisq.core.dao.state.blockchain.MutableTx;
import bisq.core.dao.state.blockchain.Tx;
import bisq.core.dao.state.blockchain.TxType;

import javax.inject.Inject;

/**
 * Verifies if a given transaction is a BSQ genesis transaction.
 */
public class GenesisTxValidator {

    private final StateService stateService;
    private final GenesisTxOutputValidator genesisTxOutputValidator;

    @Inject
    public GenesisTxValidator(StateService stateService,
                              GenesisTxOutputValidator genesisTxOutputValidator) {
        this.stateService = stateService;
        this.genesisTxOutputValidator = genesisTxOutputValidator;
    }

    public boolean validate(Tx tx, int blockHeight) {
        boolean isGenesis = blockHeight == stateService.getGenesisBlockHeight() &&
                tx.getId().equals(stateService.getGenesisTxId());
        if (isGenesis) {
            MutableTx mutableTx = new MutableTx(tx);
            mutableTx.setTxType(TxType.GENESIS);
            stateService.addMutableTx(mutableTx);

            ParsingModel parsingModel = new ParsingModel(stateService.getGenesisTotalSupply().getValue());
            for (int i = 0; i < tx.getOutputs().size(); ++i) {
                genesisTxOutputValidator.validate(tx.getOutputs().get(i), parsingModel);
            }
        }
        return isGenesis;
    }
}
