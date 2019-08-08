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
package org.openlmis.core.view.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.BaseReportPresenter;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.utils.ViewUtil;
import org.openlmis.core.view.widget.MMIAInfoList;
import org.openlmis.core.view.widget.MMIARegimeList;
import org.openlmis.core.view.widget.MMIARegimeThreeLineList;
import org.openlmis.core.view.widget.MMIARnrForm;
import org.openlmis.core.view.widget.RnrFormHorizontalScrollView;
import org.openlmis.core.view.widget.SingleClickButtonListener;

import java.util.Date;
import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

public class MMIARequisitionFragment extends BaseReportFragment implements MMIARequisitionPresenter.MMIARequisitionView {
    @InjectView(R.id.rnr_form_list)
    protected MMIARnrForm rnrFormList;

    @InjectView(R.id.mmia_regime_three_line_list)
    protected MMIARegimeThreeLineList mmiaRegimeThreeLineListView;
    @InjectView(R.id.mmia_threapeutic_layout)
    protected LinearLayout mmiaThreaPeuticLayout;

    @InjectView(R.id.regime_list)
    protected MMIARegimeList regimeListView;

    @InjectView(R.id.mmia_info_list)
    protected MMIAInfoList mmiaInfoListView;

    @InjectView(R.id.tv_regime_total)
    protected TextView tvRegimeTotal;
    @InjectView(R.id.tv_regime_total_pharmacy)
    protected TextView tvRegimeTotalPharmacy;

    @InjectView(R.id.mmia_regime_three_line_total)
    protected TextView mmiaRegimeThreeLineTotal;
    @InjectView(R.id.mmia_regime_three_line_pharmacy)
    protected TextView mmiaRegimeThreeLinePharmacy;

    @InjectView(R.id.et_comment)
    protected TextView etComment;

    @InjectView(R.id.scrollview)
    protected ScrollView scrollView;

    @InjectView(R.id.tv_total_mismatch)
    protected TextView tvMismatch;

    @InjectView(R.id.mmia_rnr_items_header_freeze)
    protected ViewGroup rnrItemsHeaderFreeze;

    @InjectView(R.id.mmia_rnr_items_header_freeze_left)
    protected ViewGroup rnrItemsHeaderFreezeLeft;

    @InjectView(R.id.mmia_rnr_items_header_freeze_right)
    protected ViewGroup rnrItemsHeaderFreezeRight;

    MMIARequisitionPresenter presenter;

    private long formId;
    protected View containerView;
    private Date periodEndDate;

    private static final String TAG_MISMATCH = "mismatch";
    public static final int REQUEST_FOR_CUSTOM_REGIME = 100;

    protected int actionBarHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        formId = getActivity().getIntent().getLongExtra(Constants.PARAM_FORM_ID, 0);
        periodEndDate = ((Date) getActivity().getIntent().getSerializableExtra(Constants.PARAM_SELECTED_INVENTORY_DATE));
    }

    @Override
    protected BaseReportPresenter injectPresenter() {
        presenter = RoboGuice.getInjector(getActivity()).getInstance(MMIARequisitionPresenter.class);
        return presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = inflater.inflate(R.layout.fragment_mmia_requisition, container, false);
        return containerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (SharedPreferenceMgr.getInstance().shouldSyncLastYearStockData()) {
            ToastUtil.showInCenter(R.string.msg_stock_movement_is_not_ready);
            finish();
            return;
        }

        initUI();
        if (isSavedInstanceState && presenter.getRnRForm() != null) {
            presenter.updateFormUI();
        } else {
            presenter.loadData(formId, periodEndDate);
        }
    }

    protected void initUI() {
        scrollView.setVisibility(View.INVISIBLE);
        if (isHistoryForm()) {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            actionPanelView.setVisibility(View.GONE);
            etComment.setEnabled(false);
        } else {
            scrollView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            actionPanelView.setVisibility(View.VISIBLE);
            etComment.setEnabled(true);
        }
        disableFreezeHeaderScroll();
        initActionBarHeight();
        setRegimenListener();
    }

    private boolean isHistoryForm() {
        return formId != 0;
    }

    private void setRegimenListener() {
        regimeListView.setRegimeListener(new MMIARegimeList.MMIARegimeListener() {
            @Override
            public void loading() {
                MMIARequisitionFragment.this.loading();
            }

            @Override
            public void loaded() {
                MMIARequisitionFragment.this.loaded();
            }
        });

        mmiaRegimeThreeLineListView.setRegimeThreeLineListener(new MMIARegimeThreeLineList.MMIARegimeThreeLineListener() {
            @Override
            public void loading() {
                MMIARequisitionFragment.this.loading();
            }

            @Override
            public void loaded() {
                MMIARequisitionFragment.this.loaded();
            }
        });
    }

    private void disableFreezeHeaderScroll() {
        rnrItemsHeaderFreezeRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void refreshRequisitionForm(RnRForm form) {
        scrollView.setVisibility(View.VISIBLE);
        rnrFormList.initView(form.getRnrFormItemListWrapper());
        List<RegimenItemThreeLines> dataList = form.getRegimenThreeLineListWrapper();
        if (dataList!=null && !dataList.isEmpty()) {
            mmiaRegimeThreeLineListView.initView(mmiaRegimeThreeLineTotal, mmiaRegimeThreeLinePharmacy, dataList);
        } else {
            mmiaThreaPeuticLayout.setVisibility(View.GONE);
            tvRegimeTotalPharmacy.setVisibility(View.GONE);
        }
        regimeListView.initView(tvRegimeTotal, tvRegimeTotalPharmacy, presenter);
        mmiaInfoListView.initView(form.getBaseInfoItemListWrapper());
        InflateFreezeHeaderView();
        getActivity().setTitle(getString(R.string.label_mmia_title, DateUtil.formatDateWithoutYear(form.getPeriodBegin()), DateUtil.formatDateWithoutYear(form.getPeriodEnd())));
        etComment.setText(form.getComments());
        highlightTotalDifference();
        bindListeners();
    }

    private void InflateFreezeHeaderView() {
        final View leftHeaderView = rnrFormList.getLeftHeaderView();
        rnrItemsHeaderFreezeLeft.addView(leftHeaderView);

        final ViewGroup rightHeaderView = rnrFormList.getRightHeaderView();
        rnrItemsHeaderFreezeRight.addView(rightHeaderView);

        rnrFormList.post(new Runnable() {
            @Override
            public void run() {
                ViewUtil.syncViewHeight(leftHeaderView, rightHeaderView);
            }
        });
    }


    protected void bindListeners() {
        etComment.addTextChangedListener(commentTextWatcher);
        actionPanelView.setListener(getOnCompleteListener(), getOnSaveListener());
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.requestFocus();
                hideImm();
                return false;
            }
        });

        bindFreezeHeaderListener();
    }

    @NonNull
    private SingleClickButtonListener getOnSaveListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                loading();
                Subscription subscription = presenter.getSaveFormObservable(rnrFormList.itemFormList,
                        regimeListView.getDataList(),
                        mmiaInfoListView.getDataList(),
                        mmiaRegimeThreeLineListView.getDataList(),
                        etComment.getText().toString())
                        .subscribe(getOnSavedSubscriber());
                subscriptions.add(subscription);
            }
        };
    }

    @NonNull
    public Subscriber<Void> getOnSavedSubscriber() {
        return new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                loaded();
                finish();
            }

            @Override
            public void onError(Throwable e) {
                loaded();
                ToastUtil.show(getString(R.string.hint_save_mmia_failed));
            }

            @Override
            public void onNext(Void aVoid) {

            }
        };
    }

    @NonNull
    private SingleClickButtonListener getOnCompleteListener() {
        return new SingleClickButtonListener() {
            @Override
            public void onSingleClick(View v) {
                if (rnrFormList.isCompleted()
                        && regimeListView.isCompleted()
                        && mmiaInfoListView.isCompleted()
                        && mmiaRegimeThreeLineListView.isCompleted()) {
                    presenter.setViewModels(rnrFormList.itemFormList,
                            regimeListView.getDataList(),
                            mmiaInfoListView.getDataList(),
                            mmiaRegimeThreeLineListView.getDataList(),
                            etComment.getText().toString());
                    if (!presenter.validateFormPeriod()) {
                        ToastUtil.show(R.string.msg_requisition_not_unique);
                    } else if (shouldCommentMandatory()) {
                        etComment.setError(getString(R.string.mmia_comment_should_not_empty));
                    } else {
                        showSignDialog();
                    }
                }
            }
        };
    }


    private boolean shouldCommentMandatory() {
        boolean isTotalEqual = Long.parseLong(mmiaRegimeThreeLineTotal.getText().toString())
                != Long.parseLong(tvRegimeTotal.getText().toString());
        boolean isPhaymacyEqual = Long.parseLong(mmiaRegimeThreeLinePharmacy.getText().toString())
                != Long.parseLong(tvRegimeTotalPharmacy.getText().toString());
        boolean isCommentEmpty = TextUtils.isEmpty(etComment.getText().toString());

        return isCommentEmpty && (isTotalEqual || isPhaymacyEqual);
    }

    private void bindFreezeHeaderListener() {
        ViewTreeObserver verticalViewTreeObserver = scrollView.getViewTreeObserver();
        verticalViewTreeObserver.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                hideOrDisplayRnrItemsHeader();
            }
        });

        rnrFormList.getRnrItemsHorizontalScrollView().setOnScrollChangedListener(new RnrFormHorizontalScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                rnrItemsHeaderFreezeRight.scrollBy(l - oldl, 0);
            }
        });
    }

    private void initActionBarHeight() {
        containerView.post(new Runnable() {
            @Override
            public void run() {
                int[] initialTopLocationOfRnrForm = new int[2];
                containerView.getLocationOnScreen(initialTopLocationOfRnrForm);
                actionBarHeight = initialTopLocationOfRnrForm[1];
            }
        });
    }

    protected void hideOrDisplayRnrItemsHeader() {
        rnrItemsHeaderFreeze.setVisibility(isNeedHideFreezeHeader() ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean isNeedHideFreezeHeader() {
        int[] rnrItemsViewLocation = new int[2];
        rnrFormList.getLocationOnScreen(rnrItemsViewLocation);
        final int rnrFormY = rnrItemsViewLocation[1];

        int lastItemHeight = rnrFormList.getRightViewGroup().getChildAt(rnrFormList.getRightViewGroup().getChildCount() - 1).getHeight();

        final int offsetY = -rnrFormY + rnrItemsHeaderFreeze.getHeight() + actionBarHeight;

        final int hiddenThresholdY = rnrFormList.getHeight() - lastItemHeight;

        return offsetY > hiddenThresholdY;
    }

    TextWatcher commentTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            presenter.setComments(s.toString());
        }
    };

    private void highlightTotalDifference() {
        regimeListView.deHighLightTotal();
        if (mmiaThreaPeuticLayout.getVisibility() != View.GONE) {
            mmiaRegimeThreeLineListView.deHighLightTotal();
        }
        mmiaInfoListView.deHighLightTotal();
        tvMismatch.setVisibility(View.INVISIBLE);
    }

    private boolean hasEmptyColumn() {
        return regimeListView.hasEmptyField() || mmiaInfoListView.hasEmptyField() || mmiaRegimeThreeLineListView.hasEmptyField();
    }

    @Override
    protected void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        super.finish();
    }

    @Override
    public void showValidationAlert() {
        DialogFragment dialogFragment = SimpleDialogFragment.newInstance(null,
                getString(R.string.msg_regime_total_and_patient_total_not_match),
                getString(R.string.btn_ok),
                TAG_MISMATCH);
        dialogFragment.show(getFragmentManager(), "not_match_dialog");
    }

    @Override
    public void setProcessButtonName(String buttonName) {
        actionPanelView.setPositiveButtonText(buttonName);
    }

    @Override
    public void completeSuccess() {
        ToastUtil.showForLongTime(R.string.msg_mmia_submit_tip);
        finish();
    }

    @NonNull
    public String getSignatureDialogTitle() {
        return presenter.isDraftOrDraftMissed() ? getResources().getString(R.string.msg_mmia_submit_signature) : getResources().getString(R.string.msg_approve_signature_mmia);
    }

    protected Action1<? super Void> getOnSignedAction() {
        return new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                if (presenter.getRnRForm().isSubmitted()) {
                    presenter.submitRequisition();
                    showMessageNotifyDialog();
                } else {
                    presenter.authoriseRequisition();
                }
            }
        };
    }

    @Override
    protected String getNotifyDialogMsg() {
        return getString(R.string.msg_requisition_signature_message_notify_mmia);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_FOR_CUSTOM_REGIME) {
            regimeListView.addCustomRegimenItem((Regimen) data.getSerializableExtra(Constants.PARAM_CUSTOM_REGIMEN));
        }
    }
}