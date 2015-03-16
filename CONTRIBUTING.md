# Reporting issues

Found a bug in the library? Don't hesitate to open an issue! But first:

* Check if it hasn't been reported yet.
* Verify the bug still exists in the `dev` branch.

When describing an issue, be precise:

* Include steps to reproduce (including sample code if necessary);
* What happened? What do you think should have happened?
* On what Android version does this issue occur?

# Asking Questions

Asking questions should preferably be done on a dedicated forum such as [StackOverflow](http://stackoverflow.com/). This is an issue tracker after all. 

# Contributing code

## Please do!

I'm happy to view and accept pull requests. However, it is important to follow these guidelines if you want to contribute.

## General steps

* [Fork](https://github.com/nhaarman/ListViewAnimations/fork) the repository.
* Create a local clone of the repository.
* Create a local branch, **based on the `dev` branch** (see the *Rules* section)
* Commit your code, and push the changes to your own repository.
* Create a pull request, specifying that you want to merge into the `dev` branch (or any child branch of it)

## Rules

* Branch names should start with either `feature_` or `bugfix_`. If there is an open issue, include its number, like `bugfix_123`.
* **Do not** include in your commit message anything related to automatic issue closing, such as `Fixes issue 123`. We'll do that when merging your pull request.
* **Do not** put any `@author` comment. Git keeps track of all your changes and `@author` does more harm than good.
* **Do not** issue a pull request into the `master` branch.
* Try to keep the diff as small as possible. For example, be aware of auto formatting.
* All files should have the Apache 2 License header.
