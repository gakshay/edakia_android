package com.gakshay.android.fileChooser;

public class Option implements Comparable<Option>{
	private String name;
	private String dateOfAccess;
	private String path;
	
	public Option(String n,String d,String p)
	{
		name = n;
		dateOfAccess = d;
		path = p;
	}
	public String getName()
	{
		return name;
	}
	public String getData()
	{
		return dateOfAccess;
	}
	public String getPath()
	{
		return path;
	}
	@Override
	public int compareTo(Option o) {
		if(this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase()); 
		else 
			throw new IllegalArgumentException();
	}
}
