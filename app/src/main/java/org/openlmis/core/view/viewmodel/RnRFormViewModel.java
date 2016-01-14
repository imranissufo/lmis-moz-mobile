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
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.repository.MMIARepository;
import org.openlmis.core.model.repository.VIARepository;
import org.openlmis.core.utils.DateUtil;

import lombok.Data;

@Data
public class RnRFormViewModel {

    public static final int TYPE_DRAFT = 1;
    public static final int TYPE_UNSYNC = 2;
    public static final int TYPE_HISTORICAL = 3;

    int type;
    String syncedDate;
    String period;
    String title;
    String name;
    long id;
    String syncServerErrorMessage;
    private RnRForm form;

    public RnRFormViewModel(RnRForm form) {
        this.form = form;
        this.syncedDate = DateUtil.formatDate(form.getUpdatedAt());
        this.period = LMISApp.getContext().getString(R.string.label_period_date, DateUtil.formatDate(form.getPeriodBegin()), DateUtil.formatDate(form.getPeriodEnd()));
        this.id = form.getId();

        setName(form);
        setType(form);
    }

    public void setType(RnRForm form) {
        if (form.getStatus() == RnRForm.STATUS.AUTHORIZED) {
            this.type = form.isSynced() ? TYPE_HISTORICAL : TYPE_UNSYNC;
        } else {
            this.type = TYPE_DRAFT;
        }
    }

    public void setSyncServerErrorMessage(String syncServerErrorMessage) {
        this.syncServerErrorMessage = syncServerErrorMessage;
    }

    private void setName(RnRForm form) {
        switch (form.getProgram().getProgramCode()) {
            case MMIARepository.MMIA_PROGRAM_CODE:
                this.name = LMISApp.getContext().getString(R.string.label_mmia_name);
                break;
            case VIARepository.VIA_PROGRAM_CODE:
                this.name = LMISApp.getContext().getString(R.string.label_via_name);
                break;
            default:
                this.name = StringUtils.EMPTY;
                break;
        }
    }

}
