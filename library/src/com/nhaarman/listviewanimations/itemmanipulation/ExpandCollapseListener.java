package com.nhaarman.listviewanimations.itemmanipulation;

// TODO integrate in ExpandableListItemAdapter
public interface ExpandCollapseListener {

    public void onItemExpanded(int position);

    public void onItemCollapsed(int position);

}
