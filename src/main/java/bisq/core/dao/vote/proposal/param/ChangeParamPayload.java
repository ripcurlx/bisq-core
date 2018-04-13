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

package bisq.core.dao.vote.proposal.param;

import bisq.network.p2p.storage.payload.ProtectedStoragePayload;

import io.bisq.generated.protobuffer.PB;

import java.security.PublicKey;

import java.util.Map;

import lombok.Value;

import javax.annotation.Nullable;

@Value
public class ChangeParamPayload implements ProtectedStoragePayload {
    private final Param param;
    private final long value;

    public ChangeParamPayload(Param param, long value) {
        this.param = param;
        this.value = value;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public PB.ChangeParamPayload toProtoMessage() {
        final PB.ChangeParamPayload.Builder builder = PB.ChangeParamPayload.newBuilder()
                .setDaoParamOrdinal(param.ordinal())
                .setValue(value);
        return builder.build();
    }

    public static ChangeParamPayload fromProto(PB.ChangeParamPayload proto) {
        return new ChangeParamPayload(Param.values()[proto.getDaoParamOrdinal()],
                proto.getValue());
    }

    @Override
    public String toString() {
        return "ChangeParamPayload{" +
                "\n     param=" + param +
                ",\n     value=" + value +
                "\n}";
    }

    @Override
    public PublicKey getOwnerPubKey() {
        return null;
    }

    @Nullable
    @Override
    public Map<String, String> getExtraDataMap() {
        return null;
    }
}
