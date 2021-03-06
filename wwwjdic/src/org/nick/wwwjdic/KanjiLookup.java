package org.nick.wwwjdic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.nick.wwwjdic.history.FavoritesAndHistorySummaryView;
import org.nick.wwwjdic.history.HistoryDbHelper;
import org.nick.wwwjdic.hkr.RecognizeKanjiActivity;
import org.nick.wwwjdic.krad.KradChart;
import org.nick.wwwjdic.ocr.OcrActivity;
import org.nick.wwwjdic.utils.Analytics;
import org.nick.wwwjdic.utils.IntentSpan;
import org.nick.wwwjdic.utils.StringUtils;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class KanjiLookup extends WwwjdicActivityBase implements
        OnClickListener, OnItemSelectedListener {

    private static final String TAG = KanjiLookup.class.getSimpleName();

    private static final int NUM_RECENT_HISTORY_ENTRIES = 5;

    private static final Map<Integer, String> IDX_TO_CODE = new HashMap<Integer, String>();

    private EditText kanjiInputText;
    private Spinner kanjiSearchTypeSpinner;

    private EditText radicalEditText;
    private Button selectRadicalButton;
    private EditText strokeCountMinInput;
    private EditText strokeCountMaxInput;

    private FavoritesAndHistorySummaryView kanjiHistorySummary;

    private HistoryDbHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kanji_lookup);

        populateIdxToCode();

        findViews();
        setupListeners();
        setupSpinners();
        setupTabOrder();
        toggleRadicalStrokeCountPanel(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String searchKey = extras.getString(Constants.SEARCH_TEXT_KEY);
            int searchType = extras.getInt(Constants.SEARCH_TYPE);
            if (searchKey != null) {
                switch (searchType) {
                case SearchCriteria.CRITERIA_TYPE_KANJI:
                    kanjiInputText.setText(searchKey);
                    break;
                default:
                    // do nothing
                }
                inputTextFromBundle = true;
            }
        }

        dbHelper = HistoryDbHelper.getInstance(this);

        setupKanjiSummary();

        setupClickableLinks();
    }

    private void populateIdxToCode() {
        if (IDX_TO_CODE.isEmpty()) {
            String[] kanjiSearchCodesArray = getResources().getStringArray(
                    R.array.kanji_search_codes_array);
            for (int i = 0; i < kanjiSearchCodesArray.length; i++) {
                IDX_TO_CODE.put(i, kanjiSearchCodesArray[i]);
            }
        }
    }

    private void setupClickableLinks() {
        View historyView = findViewById(R.id.kanji_history_summary);
        historyView.setNextFocusDownId(R.id.hwrSearchLink);

        TextView textView = (TextView) findViewById(R.id.hwrSearchLink);
        makeClickable(textView, new Intent(this, RecognizeKanjiActivity.class));
        textView.setNextFocusUpId(R.id.kanji_history_summary);
        textView.setNextFocusDownId(R.id.ocrSearchLink);

        textView = (TextView) findViewById(R.id.ocrSearchLink);
        makeClickable(textView, new Intent(this, OcrActivity.class));
        textView.setNextFocusUpId(R.id.hwrSearchLink);
        textView.setNextFocusDownId(R.id.multiRadicalSearchLink);

        textView = (TextView) findViewById(R.id.multiRadicalSearchLink);
        makeClickable(textView, new Intent(this, KradChart.class));
        textView.setNextFocusUpId(R.id.ocrSearchLink);
    }

    private void makeClickable(TextView textView, Intent intent) {
        String text = textView.getText().toString();
        SpannableString str = new SpannableString(text);
        str.setSpan(new IntentSpan(this, intent), 0, text.length(),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        textView.setText(str);
        textView.setLinkTextColor(Color.WHITE);
        MovementMethod m = textView.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (textView.getLinksClickable()) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setupKanjiSummary();

        setupClickableLinks();
    }

    private void setupKanjiSummary() {
        dbHelper.beginTransaction();
        try {
            long numAllFavorites = dbHelper.getKanjiFavoritesCount();
            List<String> recentFavorites = dbHelper
                    .getRecentKanjiFavorites(NUM_RECENT_HISTORY_ENTRIES);
            long numAllHistory = dbHelper.getKanjiHistoryCount();
            List<String> recentHistory = dbHelper
                    .getRecentKanjiHistory(NUM_RECENT_HISTORY_ENTRIES);

            kanjiHistorySummary
                    .setFavoritesFilterType(HistoryDbHelper.FAVORITES_TYPE_KANJI);
            kanjiHistorySummary
                    .setHistoryFilterType(HistoryDbHelper.HISTORY_SEARCH_TYPE_KANJI);
            kanjiHistorySummary.setRecentEntries(numAllFavorites,
                    recentFavorites, numAllHistory, recentHistory);
            dbHelper.setTransactionSuccessful();
        } finally {
            dbHelper.endTransaction();
        }
    }

    private void setupListeners() {
        View kanjiSearchButton = findViewById(R.id.kanjiSearchButton);
        kanjiSearchButton.setOnClickListener(this);

        // kanjiInputText.setOnFocusChangeListener(this);
        selectRadicalButton.setOnClickListener(this);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> kajiSearchTypeAdapter = ArrayAdapter
                .createFromResource(this, R.array.kanji_search_types_array,
                        R.layout.spinner_text);
        kajiSearchTypeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kanjiSearchTypeSpinner.setAdapter(kajiSearchTypeAdapter);
        kanjiSearchTypeSpinner.setOnItemSelectedListener(this);
    }

    private void setupTabOrder() {
        strokeCountMinInput
                .setOnEditorActionListener(new OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId,
                            KeyEvent event) {
                        switch (actionId) {
                        case EditorInfo.IME_ACTION_NEXT:
                            EditText v1 = (EditText) v
                                    .focusSearch(View.FOCUS_RIGHT);
                            if (v1 != null) {
                                if (!v1.requestFocus(View.FOCUS_RIGHT)) {
                                    throw new IllegalStateException(
                                            "unfocucsable view");
                                }
                            }
                            break;
                        default:
                            break;
                        }
                        return true;
                    }
                });
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.kanjiSearchButton:
            // hideKeyboard();

            String kanjiInput = kanjiInputText.getText().toString();
            if (TextUtils.isEmpty(kanjiInput)) {
                return;
            }

            try {
                int searchTypeIdx = kanjiSearchTypeSpinner
                        .getSelectedItemPosition();
                String searchType = IDX_TO_CODE.get(searchTypeIdx);
                Log.i(TAG, Integer.toString(searchTypeIdx));
                Log.i(TAG, "kanji search type: " + searchType);
                if (searchType == null) {
                    // reading/kanji
                    searchType = "J";
                }

                String minStr = strokeCountMinInput.getText().toString();
                String maxStr = strokeCountMaxInput.getText().toString();
                Integer minStrokeCount = tryParseInt(minStr);
                Integer maxStrokeCount = tryParseInt(maxStr);
                SearchCriteria criteria = SearchCriteria.createWithStrokeCount(
                        kanjiInput.trim(), searchType, minStrokeCount,
                        maxStrokeCount);

                Intent intent = new Intent(this, KanjiResultListView.class);
                intent.putExtra(Constants.CRITERIA_KEY, criteria);

                if (!StringUtils.isEmpty(criteria.getQueryString())) {
                    dbHelper.addSearchCriteria(criteria);
                }

                Analytics.event("kanjiSearch", this);

                startActivity(intent);
            } catch (RejectedExecutionException e) {
                Log.e(TAG, "RejectedExecutionException", e);
            }
            break;
        case R.id.selectRadicalButton:
            Intent i = new Intent(this, RadicalChart.class);

            startActivityForResult(i, Constants.RADICAL_RETURN_RESULT);
            break;
        default:
            // do nothing
        }
    }

    private Integer tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        if (requestCode == Constants.RADICAL_RETURN_RESULT) {
            if (resultCode == RESULT_OK) {
                Radical radical = (Radical) intent.getExtras().getSerializable(
                        Constants.RADICAL_KEY);
                kanjiInputText.setText(Integer.toString(radical.getNumber()));
                radicalEditText.setText(radical.getGlyph().substring(0, 1));
            }
        }
    }

    private void findViews() {
        kanjiInputText = (EditText) findViewById(R.id.kanjiInputText);
        kanjiSearchTypeSpinner = (Spinner) findViewById(R.id.kanjiSearchTypeSpinner);

        radicalEditText = (EditText) findViewById(R.id.radicalInputText);
        strokeCountMinInput = (EditText) findViewById(R.id.strokeCountMinInput);
        strokeCountMaxInput = (EditText) findViewById(R.id.strokeCountMaxInput);
        selectRadicalButton = (Button) findViewById(R.id.selectRadicalButton);

        kanjiHistorySummary = (FavoritesAndHistorySummaryView) findViewById(R.id.kanji_history_summary);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
        if (inputTextFromBundle) {
            inputTextFromBundle = false;

            return;
        }

        switch (parent.getId()) {
        case R.id.kanjiSearchTypeSpinner:
            kanjiInputText.setText("");
            kanjiInputText.requestFocus();

            // radical number or number of strokes
            if (position == 1 || position == 2) {
                kanjiInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                kanjiInputText.setInputType(InputType.TYPE_CLASS_TEXT);
            }

            if (position != 2) {
                toggleRadicalStrokeCountPanel(false);
            } else {
                toggleRadicalStrokeCountPanel(true);
            }
            break;
        }
    }

    private void toggleRadicalStrokeCountPanel(boolean isEnabled) {
        selectRadicalButton.setEnabled(isEnabled);
        strokeCountMinInput.setEnabled(isEnabled);
        strokeCountMinInput.setFocusableInTouchMode(isEnabled);
        strokeCountMaxInput.setEnabled(isEnabled);
        strokeCountMaxInput.setFocusableInTouchMode(isEnabled);
        if (!isEnabled) {
            strokeCountMinInput.setText("");
            strokeCountMaxInput.setText("");
            radicalEditText.setText("");
            kanjiInputText.requestFocus();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

}
