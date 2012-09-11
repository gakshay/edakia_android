package com.gakshay.android.fileChooser;

import java.util.List;

import com.gakshay.android.edakia.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class FileArrayAdapter extends ArrayAdapter<Option>{

	private Context c;
	private int id;
	private List<Option>items;

	public FileArrayAdapter(Context context, int textViewResourceId,
			List<Option> objects) {
		super(context, textViewResourceId, objects);
		c = context;
		id = textViewResourceId;
		items = objects;
	}
	public Option getItem(int i)
	{
		return items.get(i);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(id, null);
		}
		final Option o = items.get(position);
		if (o != null) {
			ImageView imgIcon = (ImageView)v.findViewById(R.id.docIcon);
			if("Folder".equalsIgnoreCase(o.getData())){
				imgIcon.setImageResource(R.drawable.icn_folder);
			}else if("Parent Folder".equalsIgnoreCase(o.getData())){
				imgIcon.setImageResource(R.drawable.file_browser_back_icon);
			} else if("Parent".equalsIgnoreCase(o.getData())){
				imgIcon.setImageResource(R.drawable.file_browser_back_icon);
			} else if("pdf".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_pdf);
			} else if("ppt".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName())) || "pptx".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_ppt);
			} else if("doc".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName())) || "docx".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_doc);
			} else if("jpg".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))
					|| "jpeg".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))
					|| "gif".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))
					|| "png".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_img);
			} else if("xls".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName())) || "xlsx".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_xls);
			}else if("txt".equalsIgnoreCase(MimeTypeMap.getFileExtensionFromUrl(o.getName()))){
				imgIcon.setImageResource(R.drawable.icn_txt);
			} else{
				imgIcon.setImageResource(R.drawable.icn_txt);
			}
			TextView t1 = (TextView) v.findViewById(R.id.fileName); 
			if(t1!=null)
				t1.setText(o.getName());

		}
		return v;
	}

}
