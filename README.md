ListViewAnimations ([Play Store Demo][1])
===========

ListViewAnimations is an Open Source Android library that allows developers to easily create ListViews with animations.
Feel free to use it all you want in your Android apps provided that you cite this project and include the license in your app.

Setup
-----
* In Eclipse, just import the library as an Android library project. Project > Clean to generate the binaries 
you need, like R.java, etc.
* Then, just add ListViewAnimations as a dependency to your existing project and you're good to go!

Usage
-----
Simply extend one of the `AnimationAdapter` classes, assign it to a `ListView`, and call `setListView` on the adapter.

Example:
-----

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ListView listView = (ListView) findViewById(R.id.mylistview);
		MyAnimationAdapter mAdapter = new MyAnimationAdapter(this);
		mAdapter.setListView(listView);

		mAdapter.addAll("A", "B", "C", "D", "E", "F", "G"); 
	}
	
	
	class MyAnimationAdapter extends AnimationAdapter<String>{
	
		public MyAnimationAdapter(Context context){
			super(context);
		}
		
		@Override
		protected View getItemView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if(tv == null){
				tv = new TextView(getContext());
			}
			tv.setText(getItem(position));
			return tv;
		}
		
		@Override
		protected int getRowInAnimationResId() {
			return R.anim.my_anim;
		}

		@Override
		protected long getAnimationDelayMillis() {
			return 150;
		}
	}
	
Note
-----
* Using the one of the `AnimationAdapter` classes will hide the dividers between your listitems.

Developed By
-----
* Niek Haarman

License
-----

	Copyright 2013 Niek Haarman

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

 [1]: https://play.google.com/store/apps/details?id=com.haarman.listviewanimations