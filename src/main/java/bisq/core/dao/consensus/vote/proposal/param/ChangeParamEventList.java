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

package bisq.core.dao.consensus.vote.proposal.param;

import bisq.core.dao.consensus.state.events.ParamChangeEvent;
import bisq.core.dao.consensus.vote.VoteConsensusCritical;

import bisq.common.proto.persistable.PersistableList;

import java.util.List;

public class ChangeParamEventList extends PersistableList<ParamChangeEvent> implements VoteConsensusCritical {

    ChangeParamEventList() {
        super();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    //TODO
    private ChangeParamEventList(List<ParamChangeEvent> list) {
        super(list);
    }

   /* public static PersistableEnvelope fromProto(PB.ParamChangeEventList paramChangeEventList) {
        return null;
    }*/

  /*  @Override
    public Message toProtoMessage() {
        return PB.PersistableEnvelope.newBuilder()
                .setParamChangeEventList(PB.ParamChangeEventList.newBuilder()
                        .addAllParamChangeEvent(getList().stream()
                                .map(ParamChangeEvent::toProtoMessage)
                                .collect(Collectors.toList())))
                .build();
    }

    public static PersistableEnvelope fromProto(PB.ParamChangeEventList proto) {
        return new ChangeParamEventList(new ArrayList<>(proto.getParamChangeEventList().stream()
                .map(ParamChangeEvent::fromProto)
                .collect(Collectors.toList())));
    }*/
}
