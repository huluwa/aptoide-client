package cm.aptoide.ptdev.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import cm.aptoide.ptdev.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 30-10-2013
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */
public class Wizard {

    public static ArrayList<Fragment> getWizardNewToAptoide() {
        ArrayList<Fragment> wizard = new ArrayList<Fragment>();
        wizard.add(NewToAptoide1.newInstace());
        wizard.add(NewToAptoide2.newInstace());
        wizard.add(NewToAptoide3.newInstace());
        return wizard;
    }

    public static ArrayList<Fragment> getWizardUpdate() {
        ArrayList<Fragment> wizard = new ArrayList<Fragment>();
        wizard.add(NewFeature1.newInstace());
        wizard.add(NewFeature2.newInstace());
        wizard.add(NewFeature3.newInstace());
        wizard.add(NewFeature4.newInstace());
        return wizard;
    }

    public interface WizardCallback {

        public void getActions(ArrayList<Action> actions);
    }

    public static class NewToAptoide1 extends Fragment implements WizardCallback {

        public static NewToAptoide1 newInstace() {
            NewToAptoide1 fragment = new NewToAptoide1();
            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide1");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_welcome_to_aptoide));
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_what_is_aptoide));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_aptoide);
            TextView more_text = (TextView) view.findViewById(R.id.more_text);
            more_text.setText(getString(R.string.secure_fast_reliable));
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewToAptoide2 extends Fragment implements WizardCallback {

        CheckBox add_apps;
        TextView title;
        TextView description;
        ImageView image;
        ImageView arrow;
        TextView add_more_stores;

        public static NewToAptoide2 newInstace() {
            NewToAptoide2 fragment = new NewToAptoide2();

            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide2");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            add_apps = (CheckBox) view.findViewById(R.id.add_apps);
            add_apps.setVisibility(View.VISIBLE);
            add_apps.setChecked(true);
            add_apps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((Tutorial)getActivity()).setAddDefaultRepo(isChecked);
                    Log.d("Wizard-addDefaultRepo", "isChecked? "+ isChecked);
                }
            });
            title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_add_stores));
            description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_manage_stores));
            image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_stores);

            arrow = (ImageView) view.findViewById(R.id.arrow);
            arrow.setVisibility(View.VISIBLE);
//            add_more_stores = (TextView) view.findViewById(R.id.add_more_stores);
//            add_more_stores.setVisibility(View.VISIBLE);
//            add_more_stores.setText(getString(R.string.wizard_add_more_stores));
        }

        @Override
        public void getActions(ArrayList<Action> actions) {
            if (add_apps.isChecked()) {
                actions.add(new Action() {
                    @Override
                    public void run() {
                    }
                });
            }

        }
    }

    public static class NewToAptoide3 extends Fragment implements WizardCallback {

        public static NewToAptoide3 newInstace() {
            NewToAptoide3 fragment = new NewToAptoide3();

            Bundle args = new Bundle();
            args.putString("name", "NewToAptoide3");
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_search));
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_search_local));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_search);
            TextView more_text = (TextView) view.findViewById(R.id.more_text);
            more_text.setText(getString(R.string.wizard_search_online));
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature1 extends Fragment implements WizardCallback {

        public static NewFeature1 newInstace() {
            NewFeature1 fragment = new NewFeature1();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature1");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_new_layout));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_new_layout);

        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature2 extends Fragment implements WizardCallback {

        public static NewFeature2 newInstace() {
            NewFeature2 fragment = new NewFeature2();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature2");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_rollback));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_rollback);
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_rollback_description));
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature3 extends Fragment implements WizardCallback {

        public static NewFeature3 newInstace() {
            NewFeature3 fragment = new NewFeature3();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature3");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_widget));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_widget);
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_widget_description));
            ImageView arrow = (ImageView) view.findViewById(R.id.arrow);
            arrow.setVisibility(View.VISIBLE);
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }

    public static class NewFeature4 extends Fragment implements WizardCallback {

        public static NewFeature4 newInstace() {
            NewFeature4 fragment = new NewFeature4();
            Bundle args = new Bundle();
            args.putString("name", "NewFeature4");
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(getString(R.string.wizard_new_account));
            ImageView image = (ImageView) view.findViewById(R.id.image);
            image.setImageResource(R.drawable.wizard_new_account);
            TextView description = (TextView) view.findViewById(R.id.description);
            description.setText(getString(R.string.wizard_account_description));
        }

        @Override
        public void getActions(ArrayList<Action> actions) {

        }
    }
}
