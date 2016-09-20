package codepath.com.simpletodo20;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Created a new adapter to be able to render the keyword 'urgent' in red
 */
public class UrgentTodoAdapter extends SimpleAdapter {

    //constructor
    public UrgentTodoAdapter(Context context, List<Map<String, String>> data,
                             int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //override to perform additional actions to the view
        View v = super.getView(position, convertView, parent);
        TextView text = (TextView) v.findViewById(android.R.id.text2);
        text.setTextColor(Color.rgb(204, 0, 0));//holo dark red
        return v;
    }
}
