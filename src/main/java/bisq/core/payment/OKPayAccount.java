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

package bisq.core.payment;

import bisq.core.payment.payload.OKPayAccountPayload;
import bisq.core.payment.payload.PaymentAccountPayload;
import bisq.core.payment.payload.PaymentMethod;
import io.bisq.common.locale.CurrencyUtil;
import lombok.EqualsAndHashCode;

//TODO missing support for selected trade currency
@EqualsAndHashCode(callSuper = true)
public final class OKPayAccount extends PaymentAccount {
    public OKPayAccount() {
        super(PaymentMethod.OK_PAY);
        tradeCurrencies.addAll(CurrencyUtil.getAllOKPayCurrencies());
    }

    @Override
    protected PaymentAccountPayload createPayload() {
        return new OKPayAccountPayload(paymentMethod.getId(), id);
    }

    public void setAccountNr(String accountNr) {
        ((OKPayAccountPayload) paymentAccountPayload).setAccountNr(accountNr);
    }

    public String getAccountNr() {
        return ((OKPayAccountPayload) paymentAccountPayload).getAccountNr();
    }
}
