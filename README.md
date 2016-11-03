DEPRECATED
==========
ListViewAnimations is deprecated in favor of new RecyclerView solutions.
No new development will be taking place, but the existing versions will still function normally.

Thanks for your support!

ListViewAnimations
===========

ListViewAnimations is an Open Source Android library that allows developers to easily create ListViews with animations.
Feel free to use it all you want in your Android apps provided that you cite this project and include the license in your app.

Features
-----
ListViewAnimations provides the following features:
* Appearance animations for items in `ListViews`, `GridViews`, other `AbsListViews`;
    * Built in animations include `Alpha`, `SwingRightIn`, `SwingLeftIn`, `SwingBottomIn`, `SwingRightIn` and `ScaleIn`.
	* Other animations can easily be added
    * StickyListHeaders is supported, other implementations can easily be added.
* Swipe-to-Dismiss, Swipe-To-Dismiss with contextual undo;
* Drag-and-Drop reordering;
* Animate addition of items;
* Smoothly expand your items to reveal more content;

![](https://raw.githubusercontent.com/nhaarman/ListViewAnimations/gh-pages/images/dynamiclistview.gif "DynamicListView")

Setup
-----

The library consists of separate modules:

* `lib-core`: The core of the library, and contains appearance animations.
* `lib-manipulation`: Contains the item manipulation options, such as Swipe-to-Dismiss, and Drag-and-Drop.
* `lib-core-slh`: An extension of `lib-core` to support `StickyListHeaders`.

When using `lib-manipulation` or `lib-core-slh`, `lib-core` is included as well.

Add the following to your `build.gradle`:

	repositories {
		mavenCentral()
	}
	
	dependencies {
		compile 'com.nhaarman.listviewanimations:lib-core:3.1.0@aar'
		compile 'com.nhaarman.listviewanimations:lib-manipulation:3.1.0@aar'
		compile 'com.nhaarman.listviewanimations:lib-core-slh:3.1.0@aar'
	}

**Or**:

* Download the jar files you need:
    * [`lib-core`][8]
    * [`lib-manipulation`][9]
    * [`lib-core-slh`][10]
* [Download the latest NineOldAndroids .jar file][6]
* Add the .jar files to your project's `libs` folder, or add them as external jars to your project's build path.

**Or**:

Add the following to your `pom.xml`:

	<dependency>
		<groupId>com.nhaarman.listviewanimations</groupId>
		<artifactId>lib-core</artifactId>
		<version>3.1.0</version>
	</dependency>
	<dependency>
		<groupId>com.nhaarman.listviewanimations</groupId>
		<artifactId>lib-manipulation</artifactId>
		<version>3.1.0</version>
	</dependency>
	<dependency>
		<groupId>com.nhaarman.listviewanimations</groupId>
		<artifactId>lib-core-slh</artifactId>
		<version>3.1.0</version>
	</dependency>


Getting Started
-----

* [Wiki][11]: Tutorials and examples
* Docs:
    * `lib-core` - [Javadoc][12]
    * `lib-manipulation` - [Javadoc][13]
    * `lib-core-slh` - [Javadoc][14]

Contribute
-----
Please do! I'm happy to review and accept pull requests.  
Please read [Contributing](https://github.com/nhaarman/ListViewAnimations/blob/master/CONTRIBUTING.md) before you do.

Developed By
-----
* Niek Haarman

***

Special Thanks
-----
* DevBytes - Drag-and-Drop reordering is done by a rewritten version of their [DynamicListView][5].
* Jake Warthon - To support devices pre-HC (<3.0), a copy of [NineOldAndroids][2] is included.
* [Contributors][7]

License
-----

	Copyright 2014 Niek Haarman

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
 [2]: http://nineoldandroids.com/
 [3]: http://en.wikipedia.org/wiki/Decorator_pattern
 [5]: http://youtu.be/_BZIvjMgH-Q
 [6]: https://github.com/JakeWharton/NineOldAndroids/downloads
 [7]: https://github.com/nhaarman/ListViewAnimations/graphs/contributors
 [8]: https://github.com/nhaarman/ListViewAnimations/releases/download/3.1.0/listviewanimations_lib-core_3.1.0.jar
 [9]: https://github.com/nhaarman/ListViewAnimations/releases/download/3.1.0/listviewanimations_lib-manipulation_3.1.0.jar
 [10]: https://github.com/nhaarman/ListViewAnimations/releases/download/3.1.0/listviewanimations_lib-core-slh_3.1.0.jar
 [11]: https://github.com/nhaarman/ListViewAnimations/wiki
 [12]: http://nhaarman.github.io/ListViewAnimations/javadoc/3.1.0/lib-core
 [13]: http://nhaarman.github.io/ListViewAnimations/javadoc/3.1.0/lib-manipulation
 [14]: http://nhaarman.github.io/ListViewAnimations/javadoc/3.1.0/lib-core-slh
