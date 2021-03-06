package org.openlmis.core.view.widget;

import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;
import org.openlmis.core.model.RegimenItemThreeLines;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.presenter.MMIARequisitionPresenter;
import org.openlmis.core.view.activity.DumpFragmentActivity;
import org.openlmis.core.view.fragment.SimpleDialogFragment;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class MMIARegimeListTest {

    private MMIARegimeList mmiaRegimeList;
    private MMIARequisitionPresenter presenter;
    private DumpFragmentActivity dummyActivity;

    @Before
    public void setUp() throws Exception {
        dummyActivity = Robolectric.setupActivity(DumpFragmentActivity.class);
        mmiaRegimeList = new MMIARegimeList(dummyActivity);
        presenter = mock(MMIARequisitionPresenter.class);
        mmiaRegimeList.presenter = presenter;
    }

    @Test
    public void shouldCallDeleteMethodWhenDialogPositive() throws Exception {
        mmiaRegimeList = spy(mmiaRegimeList);
        RegimenItem item = new RegimenItem();
        Observable<Void> value = Observable.create(new Observable.OnSubscribe<Void>() {

            @Override
            public void call(Subscriber<? super Void> subscriber) {
                subscriber.onCompleted();
            }
        });
        when(presenter.deleteRegimeItem(item)).thenReturn(value);

        mmiaRegimeList.showDelConfirmDialog(item);

        SimpleDialogFragment del_confirm_dialog = (SimpleDialogFragment) dummyActivity.getFragmentManager().findFragmentByTag("del_confirm_dialog");
        SimpleDialogFragment.MsgDialogCallBack mListener = del_confirm_dialog.getMListener();
        mListener.positiveClick("");

        verify(presenter).deleteRegimeItem(item);
        verify(mmiaRegimeList).refreshRegimeView();
    }

    @Test
    public void shouldNotShowTheDelIconWhenTheFormIsAuthorised() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.AUTHORIZED);

        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        regimenItems.add(regimenItem);
        rnRForm.setRegimenItemListWrapper(regimenItems);
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(RuntimeEnvironment.application), new TextView(RuntimeEnvironment.application), presenter);

        assertNull(mmiaRegimeList.getChildAt(1).findViewById(R.id.image_view_del));
    }

    @Test
    public void shouldShowTheCustomRegimenWhenTheFormIsMissedAndNotAuthorised() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT_MISSED);


        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        ArrayList<RegimenItem> regimenItems = new ArrayList<>();
        regimenItems.add(regimenItem);
        rnRForm.setRegimenItemListWrapper(regimenItems);
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(RuntimeEnvironment.application), new TextView(RuntimeEnvironment.application), presenter);

        assertEquals(4, mmiaRegimeList.getChildCount());
    }

    private List<RegimenItemThreeLines> getRegimeItemThreeLines() {
        List<RegimenItemThreeLines> list = new ArrayList<>();
        RegimenItemThreeLines firtLine = new RegimenItemThreeLines();
        firtLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_1stline));
        list.add(firtLine);
        RegimenItemThreeLines secondLine = new RegimenItemThreeLines();
        secondLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_2ndline));
        list.add(secondLine);
        RegimenItemThreeLines thirdLine = new RegimenItemThreeLines();
        thirdLine.setRegimeTypes(RuntimeEnvironment.application.getResources().getString(R.string.mmia_3rdline));
        list.add(thirdLine);
        return list;
    }

    @Test
    public void shouldShowTheDelIconWhenTheFormIsNotAuthorised() throws Exception {
        RnRForm rnRForm = new RnRForm();
        rnRForm.setStatus(RnRForm.STATUS.DRAFT);
        rnRForm.setRegimenItemListWrapper(newArrayList(generateRegimenItem()));
        rnRForm.setRegimenThreeLinesWrapper(getRegimeItemThreeLines());

        when(presenter.getRnRForm()).thenReturn(rnRForm);

        mmiaRegimeList.initView(new TextView(LMISTestApp.getContext()), new TextView(LMISTestApp.getContext()), presenter);

        assertThat(mmiaRegimeList.getChildAt(1).findViewById(R.id.image_view_del).getVisibility(), is(View.VISIBLE));
    }

    private RegimenItem generateRegimenItem() {
        Regimen regimen = new Regimen();
        regimen.setType(Regimen.RegimeType.Adults);
        regimen.setName("customName");
        regimen.setCustom(true);
        RegimenItem regimenItem = new RegimenItem();
        regimenItem.setRegimen(regimen);
        return regimenItem;
    }
}