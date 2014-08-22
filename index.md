ListViewAnimations [![Build Status](https://travis-ci.org/nhaarman/ListViewAnimations.svg?branch=master)](https://travis-ci.org/nhaarman/ListViewAnimations)  
===========
[Play Store Demo][1]


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
		compile 'com.nhaarman.listviewanimations:lib-core:3.1.0'
		compile 'com.nhaarman.listviewanimations:lib-manipulation:3.1.0'
		compile 'com.nhaarman.listviewanimations:lib-core-slh:3.1.0'
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
	
Contribute
-----
Please do! I'm happy to review and accept pull requests.  
Please read [Contributing](https://github.com/nhaarman/ListViewAnimations/blob/master/CONTRIBUTING.md) before you do.

Developed By
-----
* Niek Haarman

***

# Getting started

Assuming you have included the library as described above, the next section describes how you can get started with adding animations to your ListViews.

* [Appearance animations](http://nhaarman.github.io/ListViewAnimations#appearance-animations)
* [DynamicListView](http://nhaarman.github.io/ListViewAnimations#dynamiclistview)
  * [Drag and drop](http://nhaarman.github.io/ListViewAnimations#draganddrop)
  * [Swipe to dismiss](http://nhaarman.github.io/ListViewAnimations#swipetodismiss)
  * [Swipe to dismiss with contextual undo](http://nhaarman.github.io/ListViewAnimations#Swipetodismisswithcontextualundo)
* [StickyListHeaders](http://nhaarman.github.io/ListViewAnimations#stickylistheaders)

***

## Appearance animations

The classes in the `com.nhaarman.listviewanimations.appearance` package provide a way for you to add more fancyness to your ListViews when showing data for the first time. Instead of snapping the items into view, the `AnimationAdapter` class lets you gradually present your items to the user:

![Default behaviour on the left, animated behaviour on the right](https://raw.githubusercontent.com/nhaarman/ListViewAnimations/gh-pages/images/demo_appearance.gif)
<sub>_Default behavior on the left, animated behaviour on the right._</sub>

To implement this behaviour, you need to wrap your original adapter in an `AlphaInAnimationAdapter`:

```java
MyAdapter myAdapter = new MyAdapter();
AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(myAdapter);
animationAdapter.setAbsListView(mListView);
mListView.setAdapter(animationAdapter);
```

You can create your own `AnimationAdapter` implementation, or use one of the predefined ones:
* `AlphaAnimationAdapter`
* `ScaleInAnimationAdapter`
* `SwingBottomInAnimationAdapter`
* `SwingLeftInAnimationAdapter`
* `SwingRightInAnimationAdapter`

***

## DynamicListView

The `DynamicListView` is a convenience class which provides drag and drop, swipe to dismiss and insertion animation functionality. It has been designed to combine these features in a most optimal way:

![The DynamicListView in action](https://raw.githubusercontent.com/nhaarman/ListViewAnimations/gh-pages/images/dynamiclistview.gif)
<sub>_The DynamicListView in action._</sub>

To use the `DynamicListView`, include the following in your xml layout:

```xml
<com.nhaarman.listviewanimations.itemmanipulation.DynamicListView
        android:id="@+id/dynamiclistview"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

### Drag and drop

To enable drag and drop, simply call `enableDragAndDrop()` on your `DynamicListView`, and specify when items are draggable. To specify a child view which can be touched to initiate a drag, you can use a `TouchViewDraggableManager`:

```java
mDynamicListView.enableDragAndDrop();
mDynamicListView.setDraggableManager(new TouchViewDraggableManager(R.id.itemrow_gripview));
```

You can also initiate a drag by calling `startDragging(int)`, for example in an `OnItemLongClickListener`:

```java
mDynamicListView.enableDragAndDrop();
mDynamicListView.setOnItemLongClickListener(
    new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                       final int position, final long id) {
            mDynamicListView.startDragging(position);
            return true;
        }
    }
);
```
<sub>Note that drag and drop functionality is only available on devices running ICS (API 14) and above.</sub>

### Swipe to dismiss
To enable swipe to dismiss, call `enableSwipeToDismiss(OnDismissCallback)` on your `DynamicListView`. The `OnDismissCallback` you must supply is notified of dismissed items, and is responsible for deleting those items from the dataset:

```java
mDynamicListView.enableSwipeToDismiss(
    new OnDismissCallback() {
        @Override
        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                mAdapter.remove(position);
            }
        }
    }
);
```

### Swipe to dismiss with contextual undo
To enable swipe to dismiss with contextual undo, you can wrap your adapter in either a `SimpleSwipeUndoAdapter`, or a `TimedUndoAdapter`. The latter will automatically dismiss an item after a while when it has been brought into the undo state.

```java
MyAdapter myAdapter = new MyAdapter();
SimpleSwipeUndoAdapter swipeUndoAdapter = new SimpleSwipeUndoAdapter(myAdapter, MyActivity.this,
    new OnDismissCallback() {
        @Override
        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                mAdapter.remove(position);
            }
        }
    }
);
swipeUndoAdapter.setAbsListView(mDynamicListView);
mDynamicListView.setAdapter(swipeUndoAdapter);
mDynamicListView.enableSimpleSwipeUndo();
```

### Animate addition
The `DynamicListView` can also animate the addition of items in your dataset. To use this functionality, simply let your adapter implement `Insertable`, and call one of the `insert` methods on the `DynamicListView`:

```java
MyInsertableAdapter myAdapter = new MyInsertableAdapter(); // MyInsertableAdapter implements Insertable
mDynamicListView.setAdapter(myAdapter);
mDynamicListView.insert(0, myItem); // myItem is of the type the adapter represents.
```

***

## StickyListHeaders

ListViewAnimations also supports appearance animations on `StickyListHeaderListView`s. You must wrap your `AnimationAdapter` in a `StickyListHeadersAdapterDecorator`:

```java
StickyListHeadersListView listView = (...);
AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
StickyListHeadersAdapterDecorator stickyListHeadersAdapterDecorator = new StickyListHeadersAdapterDecorator(animationAdapter);
stickyListHeadersAdapterDecorator.setStickyListHeadersListView(listView);
listView.setAdapter(stickyListHeadersAdapterDecorator);
```

Just like with the normal `ListView`, you can use any implementation of the `AnimationAdapter` class.

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
