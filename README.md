ListViewAnimations ([Play Store Demo][1])
===========

ListViewAnimations is an Open Source Android library that allows developers to easily create ListViews with animations.
Feel free to use it all you want in your Android apps provided that you cite this project and include the license in your app.

Known applications using ListViewAnimations
-----
* Ultimate Tic-Tac-Toe ([Play Store][12])
* Light Flow Lite - LED Control ([Play Store][18])
* TreinVerkeer ([Play Store][6])
* Running Coach ([Play Store][9])
* Pearl Jam Lyrics ([Play Store][19])
* Calorie Chart ([Play Store][20])
* Car Hire ([Play Store][10])
* Super BART ([Play Store][11])
* DK FlashCards ([Play Store][15])
* Counter Plus (Tally Counter) ([Play Store][22])
* SimpleNews - RSS Reader ([Play Store][23])([Github][24])
* Voorlees Verhaaltjes 2.0 ([Play Store][21])

If you want your app to be listed as well, just open an issue, stating your app name and a link to your app, and label it 'knownApp'.

Features
-----
ListViewAnimations provides the following features:
* Appearance animations for items in ListViews, GridViews, and other AbsListViews;
    * Built in animations include Alpha, SwingRightIn, SwingLeftIn, SwingBottomIn, SwingRightIn and ScaleIn.
	* Other animations can easily be added
* Swipe-to-Dismiss, Swipe-To-Dismiss with contextual undo (and optionally count down);
* Drag-and-Drop reordering;
* Animate dismissal of items;
* Animate addition of items;
* Smoothly expand your items to reveal more content;

Setup
-----
* In Eclipse, just import the library as an Android library project.
* Project > Clean to generate the binaries you need, like R.java, etc.
* Then, just add ListViewAnimations as a dependency to your existing project and you're good to go!

**Or**:

* [Download the .jar file][4]
* [Download the latest NineOldAndroids .jar file][17]
* Add the .jar files to your project's `libs` folder, or add them as external jars to your project's build path.

**Or**:

Add the following to your `build.gradle`:

	repositories {
		mavenCentral()
	}
	
	dependencies{
		compile 'com.nhaarman.listviewanimations:library:2.6.0'
	}

**Or**:

Add the following to your `pom.xml`:

	<dependency>
		<groupId>com.nhaarman.listviewanimations</groupId>
		<artifactId>library</artifactId>
		<version>2.6.0</version>
	</dependency>
	
Usage
-----
Please refer to the [Wiki][13] pages to learn more about how to use this library.

Contribute
-----
Please do! I'm happy to review and accept pull requests.

Developed By
-----
* Niek Haarman

Special Thanks
-----
* Roman Nurik - The ListViewAnimations library uses a modified version of his [SwipeDismissListViewTouchListener][5] to support swipe-to-dismiss.
* DevBytes - Drag-and-Drop reordering is done by a modified version of their [DynamicListView][16].
* Jake Warthon - To support devices pre-HC (<3.0), a copy of [NineOldAndroids][2] is included.
* [Contributors][25]

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
 [2]: http://nineoldandroids.com/
 [3]: http://en.wikipedia.org/wiki/Decorator_pattern
 [4]: https://github.com/nhaarman/ListViewAnimations/blob/master/com.haarman.listviewanimations-2.6.0.jar?raw=true
 [5]: https://gist.github.com/romannurik/2980593
 [6]: https://play.google.com/store/apps/details?id=com.haarman.treinverkeer
 [7]: https://www.twitter.com/niekfct
 [8]: https://plus.google.com/106017817931984343451
 [9]: https://play.google.com/store/apps/details?id=com.niek.runningapp
 [10]: https://play.google.com/store/apps/details?id=com.rentalcars.handset
 [11]: https://play.google.com/store/apps/details?id=com.getgoodcode.bart
 [12]: https://play.google.com/store/apps/details?id=com.haarman.ultimatettt
 [13]: https://github.com/nhaarman/ListViewAnimations/wiki
 [15]: https://play.google.com/store/apps/details?id=com.ducky.flashcards
 [16]: http://youtu.be/_BZIvjMgH-Q
 [17]: https://github.com/JakeWharton/NineOldAndroids/downloads
 [18]: https://play.google.com/store/apps/details?id=com.rageconsulting.android.lightflowlite
 [19]: https://play.google.com/store/apps/details?id=com.juannale.pearljamlyricsapp
 [20]: https://play.google.com/store/apps/details?id=com.cafetaso.foodinfo
 [21]: https://play.google.com/store/apps/details?id=sa.voorleesVerhaaltjes
 [22]: https://play.google.com/store/apps/details?id=com.seedform.counter
 [23]: https://play.google.com/store/apps/details?id=de.dala.simplenews
 [24]: https://github.com/Dalanie/SimpleNews
 [25]: https://github.com/nhaarman/ListViewAnimations/graphs/contributors