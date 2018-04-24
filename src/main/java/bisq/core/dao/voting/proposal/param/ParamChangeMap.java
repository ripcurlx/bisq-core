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

package bisq.core.dao.voting.proposal.param;

import bisq.common.proto.persistable.PersistablePayload;

import io.bisq.generated.protobuffer.PB;

import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;

import javax.annotation.concurrent.Immutable;

// PB does not support lists inside a map so we use that wrapper class.
@Immutable
@Value
public class ParamChangeMap implements PersistablePayload {
    private final Map<Param, Long> map;

    public ParamChangeMap(Map<Param, Long> map) {
        this.map = map;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public PB.ParamChangeMap toProtoMessage() {
        return PB.ParamChangeMap.newBuilder()
                .putAllMap(map.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().ordinal(), Map.Entry::getValue)))
                .build();
    }

    public static ParamChangeMap fromProto(PB.ParamChangeMap proto) {
        return new ParamChangeMap(proto.getMapMap().entrySet().stream()
                .collect(Collectors.toMap(e -> Param.values()[e.getKey()], Map.Entry::getValue)));
    }
}
