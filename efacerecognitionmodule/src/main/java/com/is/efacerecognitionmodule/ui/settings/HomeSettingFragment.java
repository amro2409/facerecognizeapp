package com.is.efacerecognitionmodule.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.is.efacerecognitionmodule.MainActivity;
import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.utils.LanguageHelper;

/**
 * A simple {@link Fragment} subclass.
 *
 * create an instance of this fragment.
 */
public class HomeSettingFragment extends android.app.Fragment implements AdapterView.OnItemClickListener {

    private static final Class<?>[] CLASSES = new Class<?>[]{LocationPreferenceFragment.class};
    private ListView listView;

    public HomeSettingFragment() {
        // Required empty public constructor
    }
    SettingsActivity settingsActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsActivity= (SettingsActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_setting, container, false);
    }

    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = view.findViewById(R.id.list_view);
        String[] dataItems = {getString(R.string.setting_location), getString(R.string.setting_period_work),
                getString(R.string.setting_management_account), getString(R.string.setting_language)};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, dataItems);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(this);
        listView.setScrollIndicators(View.SCROLL_INDICATOR_BOTTOM);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String txt= (String) parent.getItemAtPosition(position);
        if (getString(R.string.setting_language).equals(txt)){
            popOptionLanguage(view);

            return;
        }
        if (CLASSES.length > position) {
            settingsActivity.startFragment(CLASSES[position]);
        }

        listView.setSelection(position);

    }


    private void popOptionLanguage(View view) {
        @SuppressLint({"NewApi", "LocalSuppress"})
            final android.widget.PopupMenu popup = new android.widget.PopupMenu(getContext(), view);
            popup.setOnMenuItemClickListener(
                    menuItem -> {
                        int itemId = menuItem.getItemId();
                        if (itemId == R.id.arb_lang) {
                            recreateActivityToUpdateLang("ar");
                            return true;
                        } else if (itemId ==R.id.en_lang) {
                            recreateActivityToUpdateLang("en");
                            return true;
                        }
                        return false;
                    });
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.language_menu, popup.getMenu());
            popup.show();
    }
    private void recreateActivityToUpdateLang(String langCode){
        LanguageHelper.setLocale(getActivity(), langCode);

        Intent intent = new Intent(getActivity(), MainActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}