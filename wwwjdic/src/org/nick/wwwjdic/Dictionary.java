package org.nick.wwwjdic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.nick.wwwjdic.history.FavoritesAndHistorySummaryView;
import org.nick.wwwjdic.history.HistoryDbHelper;
import org.nick.wwwjdic.utils.Analytics;
import org.nick.wwwjdic.utils.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class Dictionary extends WwwjdicActivityBase implements OnClickListener,
        OnFocusChangeListener, OnCheckedChangeListener, OnItemSelectedListener {

    private static final String TAG = Dictionary.class.getSimpleName();

    private static final Map<Integer, String> IDX_TO_DICT_CODE = new HashMap<Integer, String>();

    private EditText inputText;
    private CheckBox exactMatchCb;
    private CheckBox commonWordsCb;
    private CheckBox romanizedJapaneseCb;
    private Spinner dictSpinner;

    private FavoritesAndHistorySummaryView dictHistorySummary;

    private HistoryDbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dict_lookup);

        populateIdxToDictCode();

        findViews();
        setupListeners();
        setupSpinners();

        inputText.requestFocus();
        selectDictionary(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String searchKey = extras.getString(Constants.SEARCH_TEXT_KEY);
            int searchType = extras.getInt(Constants.SEARCH_TYPE);
            if (searchKey != null) {
                switch (searchType) {
                case SearchCriteria.CRITERIA_TYPE_DICT:
                    inputText.setText(searchKey);
                    break;
                default:
                    // do nothing
                }
                inputTextFromBundle = true;
            }
        }

        dbHelper = HistoryDbHelper.getInstance(this);

        setupDictSummary();

        // delay focus request a bit, otherwise may fail
        // Cf. http://code.google.com/p/android/issues/detail?id=2705
        inputText.post(new Runnable() {
            public void run() {
                inputText.requestFocus();
            }
        });
    }

    private void populateIdxToDictCode() {
        if (IDX_TO_DICT_CODE.isEmpty()) {
            String[] dictionaryIdxs = getResources().getStringArray(
                    R.array.dictionary_idxs_array);
            String[] dictionaryCodes = getResources().getStringArray(
                    R.array.dictionary_codes_array);
            for (int i = 0; i < dictionaryIdxs.length; i++) {
                IDX_TO_DICT_CODE.put(Integer.parseInt(dictionaryIdxs[i]),
                        dictionaryCodes[i]);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void selectDictionary(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int idx = savedInstanceState.getInt(
                    Constants.SELECTED_DICTIONARY_IDX, 0);
            dictSpinner.setSelection(idx);
        } else {
            dictSpinner.setSelection(WwwjdicPreferences
                    .getDefaultDictionaryIdx(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupDictSummary();

        // selectDictionary();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Constants.SELECTED_DICTIONARY_IDX,
                dictSpinner.getSelectedItemPosition());
    }

    private void findViews() {
        inputText = (EditText) findViewById(R.id.inputText);
        exactMatchCb = (CheckBox) findViewById(R.id.exactMatchCb);
        commonWordsCb = (CheckBox) findViewById(R.id.commonWordsCb);
        romanizedJapaneseCb = (CheckBox) findViewById(R.id.romanizedCb);
        dictSpinner = (Spinner) findViewById(R.id.dictionarySpinner);

        dictHistorySummary = (FavoritesAndHistorySummaryView) findViewById(R.id.dict_history_summary);
    }

    private void setupListeners() {
        View translateButton = findViewById(R.id.translateButton);
        translateButton.setOnClickListener(this);

        // inputText.setOnFocusChangeListener(this);

        romanizedJapaneseCb.setOnCheckedChangeListener(this);
        exactMatchCb.setOnCheckedChangeListener(this);
        commonWordsCb.setOnCheckedChangeListener(this);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.dictionaries_array, R.layout.spinner_text);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dictSpinner.setAdapter(adapter);
        dictSpinner.setOnItemSelectedListener(this);
    }

    private void setupDictSummary() {
        dbHelper.beginTransaction();
        try {
            long numAllFavorites = dbHelper.getDictFavoritesCount();
            List<String> recentFavorites = dbHelper
                    .getRecentDictFavorites(NUM_RECENT_HISTORY_ENTRIES);
            long numAllHistory = dbHelper.getDictHistoryCount();
            List<String> recentHistory = dbHelper
                    .getRecentDictHistory(NUM_RECENT_HISTORY_ENTRIES);
            dictHistorySummary
                    .setFavoritesFilterType(HistoryDbHelper.FAVORITES_TYPE_DICT);
            dictHistorySummary
                    .setHistoryFilterType(HistoryDbHelper.HISTORY_SEARCH_TYPE_DICT);
            dictHistorySummary.setRecentEntries(numAllFavorites,
                    recentFavorites, numAllHistory, recentHistory);
            dbHelper.setTransactionSuccessful();
        } finally {
            dbHelper.endTransaction();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.translateButton:
            hideKeyboard();

            String input = inputText.getText().toString();
            if (TextUtils.isEmpty(input)) {
                return;
            }

            try {
                int dictIdx = dictSpinner.getSelectedItemPosition();
                String dict = getDictionaryFromSelection(dictIdx);

                SearchCriteria criteria = SearchCriteria.createForDictionary(
                        input.trim(), exactMatchCb.isChecked(),
                        romanizedJapaneseCb.isChecked(),
                        commonWordsCb.isChecked(), dict);

                Intent intent = new Intent(this, DictionaryResultListView.class);
                intent.putExtra(Constants.CRITERIA_KEY, criteria);

                if (!StringUtils.isEmpty(criteria.getQueryString())) {
                    dbHelper.addSearchCriteria(criteria);
                }

                Analytics.event("dictSearch", this);

                startActivity(intent);
            } catch (RejectedExecutionException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            break;
        default:
            // do nothing
        }
    }

    private String getDictionaryFromSelection(int dictIdx) {
        String dict = IDX_TO_DICT_CODE.get(dictIdx);
        Log.i(TAG, "dictionary idx: " + Integer.toString(dictIdx));
        Log.i(TAG, "dictionary: " + dict);
        if (dict == null) {
            // edict
            dict = "1";
        }

        return dict;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
        case R.id.exactMatchCb:
            toggleRomanizedCb(isChecked);
            break;
        case R.id.commonWordsCb:
            toggleRomanizedCb(isChecked);
            break;
        case R.id.romanizedCb:
            toggleExactCommonCbs(isChecked);
            break;
        default:
            // do nothing
        }
    }

    private void toggleExactCommonCbs(boolean isChecked) {
        if (isChecked) {
            exactMatchCb.setEnabled(false);
            commonWordsCb.setEnabled(false);
        } else {
            exactMatchCb.setEnabled(true);
            commonWordsCb.setEnabled(true);
        }
    }

    private void toggleRomanizedCb(boolean isChecked) {
        if (isChecked) {
            romanizedJapaneseCb.setEnabled(false);
        } else {
            if (!exactMatchCb.isChecked() && !commonWordsCb.isChecked()) {
                romanizedJapaneseCb.setEnabled(true);
            }
        }
    }

    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
        case R.id.inputText:
            if (hasFocus) {
                showKeyboard();
            } else {
                hideKeyboard();
            }
            break;
        default:
            // do nothing
        }
    }

    private void hideKeyboard() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
    }

    private void showKeyboard() {
        EditText editText = (EditText) findViewById(R.id.inputText);
        editText.requestFocus();
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,
            long id) {
        String dict = getDictionaryFromSelection(pos);
        String dictName = (String) parent.getSelectedItem();
        getApp().setCurrentDictionary(dict);
        getApp().setCurrentDictionaryName(dictName);
        Log.d(TAG, String.format("current dictionary: %s(%s)", dictName, dict));
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
