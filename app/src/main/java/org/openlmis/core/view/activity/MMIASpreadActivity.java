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

package org.openlmis.core.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;
import android.widget.ListView;

import com.google.inject.Inject;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIAFormPresenter;
import org.openlmis.core.presenter.Presenter;
import org.openlmis.core.view.adapter.MMIAInfoListAdapter;
import org.openlmis.core.view.adapter.RegimeListAdapter;
import org.openlmis.core.view.adapter.RnrFromListAdapter;

import java.util.ArrayList;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_mmia_spread)
public class MMIASpreadActivity extends BaseActivity {

    @InjectView(R.id.rnr_from_list)
    public ListView rnrFromListView;

    @InjectView(R.id.regime_list)
    public ListView regimeListView;


    @InjectView(R.id.mmia_info_list)
    public ListView mmiaInfoListView;

    @InjectView(R.id.ly_comment)
    public TextInputLayout ly_comment;

    @InjectView(R.id.tx_comment)
    public EditText tx_comment;

    @Inject
    MMIAFormPresenter presenter;

    @Override
    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        RnRForm rnRForm = presenter.initMIMIA();

        rnrFromListView.setAdapter(new RnrFromListAdapter(this, new ArrayList(rnRForm.getRnrFormItemList())));

        RegimeListAdapter regimeListAdapter = new RegimeListAdapter(this, new ArrayList(rnRForm.getRegimenItemList()));
        regimeListView.setAdapter(regimeListAdapter);

        mmiaInfoListView.setAdapter(new MMIAInfoListAdapter(this, new ArrayList(rnRForm.getBaseInfoItemList())));


    }

    public static Intent getIntent2Me(Context mContext) {
        Intent intent = new Intent(mContext, MMIASpreadActivity.class);
        return intent;
    }
}
