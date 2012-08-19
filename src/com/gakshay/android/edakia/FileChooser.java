package com.gakshay.android.edakia;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.gakshay.android.fileChooser.FileArrayAdapter;
import com.gakshay.android.fileChooser.Option;

public class FileChooser extends ListActivity {
	
    private File currentDir;
    private FileArrayAdapter adapter;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private static List<String> skipFiles;
    static {
    skipFiles = new ArrayList<String>();
    skipFiles.add("Android");
    skipFiles.add("LOST.DIR");
    skipFiles.add(".android_secure");

    }
   
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentDir = new File("/sdcard/");//change the directory here.
        fill(currentDir);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_view, menu);
        return true;
    }
    private void fill(File f)
    {
    	File[]dirs = f.listFiles();
		 this.setTitle("All your files: "+f.getName());
		 List<Option>dir = new ArrayList<Option>();
		 List<Option>fls = new ArrayList<Option>();
		 try{
			 for(File ff: dirs)
			 {
				 if(!skipFiles.contains(ff.getName())){
					 if(ff.isDirectory())
							dir.add(new Option(ff.getName(),"This is a folder.",ff.getAbsolutePath()));
						else
						{
							fls.add(new Option(ff.getName(),"Last Access Date : "+ dateFormatter.format(new Date(ff.lastModified()))  ,ff.getAbsolutePath()));
						} 
				 }
				
			 }
		 }catch(Exception e)
		 {
			 
		 }
		 Collections.sort(dir);
		 Collections.sort(fls);
		 dir.addAll(fls);
		 if(!f.getName().equalsIgnoreCase("sdcard"))
			 dir.add(0,new Option("..","Parent Folder.",f.getParent()));
		 adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
		 this.setListAdapter(adapter);
    }
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Option o = adapter.getItem(position);
		if(o.getData().equalsIgnoreCase("This is a folder.")||o.getData().equalsIgnoreCase("Parent Folder.")){
				currentDir = new File(o.getPath());
				fill(currentDir);
		}
		else
		{
			onFileClick(o);
		}
	}
    private void onFileClick(Option o)
    {
    	Toast.makeText(this, "File selected for Scanning : "+o.getName(), Toast.LENGTH_LONG).show();
    	Intent returnData = new Intent();
    	returnData.putExtra("filePath",o.getPath());
    	setResult(Activity.RESULT_OK,returnData);
    	finish();
    	
    }
}