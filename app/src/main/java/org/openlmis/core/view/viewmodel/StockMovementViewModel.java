/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.view.viewmodel;


import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.exceptions.MovementReasonNotFoundException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;

import java.text.ParseException;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockMovementViewModel {

    String movementDate;
    MovementReasonManager.MovementReason reason;
    String documentNo;
    String received;
    String negativeAdjustment;
    String positiveAdjustment;
    String issued;
    String stockExistence;

    boolean isDraft = true;


    public StockMovementViewModel(StockMovementItem item){
        movementDate = DateUtil.formatDate(item.getMovementDate());
        documentNo = item.getDocumentNumber();

        try {
            reason = MovementReasonManager.getInstance().queryByCode(item.getReason());
        }catch (MovementReasonNotFoundException e){
            throw new RuntimeException("MovementReason Cannot be find" + e.getMessage());
        }
        isDraft = false;

        switch (item.getMovementType()) {
            case RECEIVE:
                received = String.valueOf(item.getMovementQuantity());
                break;
            case ISSUE:
                issued = String.valueOf(item.getMovementQuantity());
                break;
            case NEGATIVE_ADJUST:
                negativeAdjustment = String.valueOf(item.getMovementQuantity());
                break;
            case POSITIVE_ADJUST:
                positiveAdjustment = String.valueOf(item.getMovementQuantity());
                break;
            default:
        }
        stockExistence = String.valueOf(item.getStockOnHand());
    }

    public StockMovementItem convertViewToModel() {
        StockMovementItem stockMovementItem = new StockMovementItem();
        stockMovementItem.setStockOnHand(Long.parseLong(getStockExistence()));

        stockMovementItem.setReason(reason.getCode());
        stockMovementItem.setDocumentNumber(getDocumentNo());
        stockMovementItem.setMovementType(reason.getMovementType());

        switch (reason.getMovementType()) {
            case ISSUE:
                stockMovementItem.setMovementQuantity(Long.parseLong(issued));
                break;
            case RECEIVE:
                stockMovementItem.setMovementQuantity(Long.parseLong(received));
                break;
            case NEGATIVE_ADJUST:
                stockMovementItem.setMovementQuantity(Long.parseLong(negativeAdjustment));
                break;
            case POSITIVE_ADJUST:
                stockMovementItem.setMovementQuantity(Long.parseLong(positiveAdjustment));
                break;
        }
        try {
            stockMovementItem.setMovementDate(DateUtil.parseString(getMovementDate(), DateUtil.DEFAULT_DATE_FORMAT));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stockMovementItem;
    }

    public boolean validateEmpty() {
        return ((reason != null
                && StringUtils.isNoneEmpty(movementDate)
                && !(StringUtils.isEmpty(received)
                && (StringUtils.isEmpty(negativeAdjustment)
                && StringUtils.isEmpty(positiveAdjustment)
                && StringUtils.isEmpty(issued))))
        );
    }

    public boolean validateInputValid() {
        return ((StringUtils.isNumeric(received)
                || StringUtils.isNumeric(negativeAdjustment)
                || StringUtils.isNumeric(positiveAdjustment)
                || StringUtils.isNumeric(issued))
                && Long.parseLong(stockExistence) >= 0);
    }
}
