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

package bisq.core.dao.state.period;

import bisq.core.dao.state.StateService;
import bisq.core.dao.state.blockchain.Tx;

import com.google.inject.Inject;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PeriodService {
    private final StateService stateService;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public PeriodService(StateService stateService) {
        this.stateService = stateService;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    public List<Cycle> getCycles() {
        return stateService.getCycles();
    }

    public Cycle getCurrentCycle() {
        return stateService.getCurrentCycle();
    }

    public int getChainHeight() {
        return stateService.getChainHeight();
    }

    public Optional<Tx> getOptionalTx(String txId) {
        return stateService.getTx(txId);
    }

    public DaoPhase.Phase getCurrentPhase() {
        return getCurrentCycle().getPhaseForHeight(this.getChainHeight()).get();
    }

    public boolean isFirstBlockInCycle(int height) {
        return getCycle(height)
                .filter(cycle -> cycle.getHeightOfFirstBlock() == height)
                .isPresent();
    }

    public boolean isLastBlockInCycle(int height) {
        return getCycle(height)
                .filter(cycle -> cycle.getHeightOfLastBlock() == height)
                .isPresent();
    }

    public Optional<Cycle> getCycle(int height) {
        return getCycles().stream()
                .filter(cycle -> cycle.getHeightOfFirstBlock() <= height)
                .filter(cycle -> cycle.getHeightOfLastBlock() >= height)
                .findAny();
    }

    public boolean isInPhase(int height, DaoPhase.Phase phase) {
        return getCycle(height)
                .filter(cycle -> cycle.isInPhase(height, phase))
                .isPresent();
    }

    public boolean isTxInPhase(String txId, DaoPhase.Phase phase) {
        return getOptionalTx(txId)
                .filter(tx -> isInPhase(tx.getBlockHeight(), phase))
                .isPresent();
    }

    public DaoPhase.Phase getPhaseForHeight(int height) {
        return getCycle(height)
                .flatMap(cycle -> cycle.getPhaseForHeight(height))
                .orElse(DaoPhase.Phase.UNDEFINED);
    }

    public boolean isTxInCorrectCycle(int txHeight, int chainHeadHeight) {
        return getCycle(txHeight)
                .filter(cycle -> chainHeadHeight >= cycle.getHeightOfFirstBlock())
                .filter(cycle -> chainHeadHeight <= cycle.getHeightOfLastBlock())
                .isPresent();
    }

    public boolean isTxInCorrectCycle(String txId, int chainHeadHeight) {
        return getOptionalTx(txId)
                .filter(tx -> isTxInCorrectCycle(tx.getBlockHeight(), chainHeadHeight))
                .isPresent();
    }

    public boolean isTxInPastCycle(int txHeight, int chainHeadHeight) {
        return getCycle(txHeight)
                .filter(cycle -> chainHeadHeight > cycle.getHeightOfLastBlock())
                .isPresent();
    }

    public int getDurationForPhase(DaoPhase.Phase phase, int height) {
        return getCycle(height)
                .map(cycle -> cycle.getDurationOfPhase(phase))
                .orElse(0);
    }

    public boolean isTxInPastCycle(String txId, int chainHeadHeight) {
        return getOptionalTx(txId)
                .filter(tx -> isTxInPastCycle(tx.getBlockHeight(), chainHeadHeight))
                .isPresent();
    }

    public int getFirstBlockOfPhase(int height, DaoPhase.Phase phase) {
        return getCycle(height)
                .map(cycle -> cycle.getFirstBlockOfPhase(phase))
                .orElse(0);
    }

    public boolean isFirstBlockInCycle() {
        final int chainHeight = getChainHeight();
        return getFirstBlockOfPhase(chainHeight, DaoPhase.Phase.PROPOSAL) == chainHeight;
    }

    public int getLastBlockOfPhase(int height, DaoPhase.Phase phase) {
        return getCycle(height)
                .map(cycle -> cycle.getLastBlockOfPhase(phase))
                .orElse(0);
    }

    public boolean isInPhaseButNotLastBlock(DaoPhase.Phase phase) {
        final int chainHeight = getChainHeight();
        return isInPhase(chainHeight, phase) &&
                chainHeight != getLastBlockOfPhase(chainHeight, phase);
    }
}
