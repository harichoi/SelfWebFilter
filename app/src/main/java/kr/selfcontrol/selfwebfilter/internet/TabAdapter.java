package kr.selfcontrol.selfwebfilter.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.selfcontrol.selfwebfilter.R;

/**
 * Created by owner on 2015-12-18.
 */

public class TabAdapter extends BaseAdapter {

    private LayoutInflater inflater = null;
    private List<InternetFragment> fragmentList = null;
    private ViewHolder viewHolder = null;
    private Context mContext = null;
    OnTabAdapterListener mListener;

    public interface OnTabAdapterListener{
        public void onDeleteTabListener(InternetFragment fragment);
        public void onClickTabListener(InternetFragment fragment);
    }

    public void setListener(OnTabAdapterListener mListener){
        this.mListener=mListener;
    }

    public TabAdapter(Context c , List<InternetFragment> arrays){
        this.mContext = c;
        this.inflater = LayoutInflater.from(c);
        this.fragmentList = arrays;
    }

    // Adapter가 관리할 Data의 개수를 설정 합니다.
    @Override
    public int getCount() {
        return fragmentList.size();
    }

    // Adapter가 관리하는 Data의 Item 의 Position을 <객체> 형태로 얻어 옵니다.
    @Override
    public InternetFragment getItem(int position) {
        return fragmentList.get(position);
    }

    // Adapter가 관리하는 Data의 Item 의 position 값의 ID 를 얻어 옵니다.
    @Override
    public long getItemId(int position) {
        return position;
    }

    // ListView의 뿌려질 한줄의 Row를 설정 합니다.
    @Override
    public View getView(int position, View convertview, ViewGroup parent) {

        View v = convertview;

        if(v == null){
            viewHolder = new ViewHolder();
            v = inflater.inflate(R.layout.internet_tab, null);
            viewHolder.internetTitle = (TextView)v.findViewById(R.id.internet_title);
            viewHolder.delTab=(Button)v.findViewById(R.id.del_tab);
            viewHolder.goTabButton=(ImageButton) v.findViewById(R.id.go_tab);
            viewHolder.fragment=getItem(position);
            v.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder)v.getTag();
        }
        InternetFragment fragment=getItem(position);

        try {
            fragment.webView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(fragment.webView.getDrawingCache());
            fragment.webView.setDrawingCacheEnabled(false);
            viewHolder.goTabButton.setImageBitmap(bitmap);
        }catch(Exception exc){

        }

        viewHolder.internetTitle.setText(fragment.getTitle());

        // image 나 button 등에 Tag를 사용해서 position 을 부여해 준다.
        // Tag란 View를 식별할 수 있게 바코드 처럼 Tag를 달아 주는 View의 기능
        // 이라고 생각 하시면 됩니다.
        //viewHolder.internet_title.setTag(position);
        //viewHolder.internet_title.setOnClickListener(buttonClickListener);
        viewHolder.delTab.setTag(fragment);
        viewHolder.delTab.setOnClickListener(buttonClickListener);
        viewHolder.goTabButton.setTag(fragment);
        viewHolder.goTabButton.setOnClickListener(buttonClickListener);

        return v;
    }

    // Adapter가 관리하는 Data List를 교체 한다.
    // 교체 후 Adapter.notifyDataSetChanged() 메서드로 변경 사실을
    // Adapter에 알려 주어 ListView에 적용 되도록 한다.
    public void setArrayList(ArrayList<InternetFragment> arrays){
        this.fragmentList = arrays;
    }

    public List<InternetFragment> getArrayList(){
        return fragmentList;
    }

    private View.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder viewHolder;
            switch (v.getId()) {
                case R.id.del_tab:
                    mListener.onDeleteTabListener((InternetFragment)v.getTag());
                    notifyDataSetChanged();
                    break;
                default:
                    mListener.onClickTabListener((InternetFragment)v.getTag());
                    notifyDataSetChanged();
                    break;
            }
        }
    };

    /*
     * ViewHolder
     * getView의 속도 향상을 위해 쓴다.
     * 한번의 findViewByID 로 재사용 하기 위해 viewHolder를 사용 한다.
     */
    public class ViewHolder{
        public TextView internetTitle = null;
        public Button delTab=null;
        public InternetFragment fragment;
        public ImageButton goTabButton=null;
        Bitmap screenShot;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
