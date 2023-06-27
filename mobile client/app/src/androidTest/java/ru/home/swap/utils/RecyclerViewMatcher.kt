package ru.home.swap.utils

import android.view.View
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.view.View.MeasureSpec.UNSPECIFIED
import androidx.core.util.Preconditions
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import ru.home.swap.ui.profile.ItemAdapter

/**
 * Credits to SO user
 *
 * @link https://stackoverflow.com/a/64241245/3649629
 * */
class RecyclerViewMatcher(private val recyclerId: Int) {

    fun atPosition(position: Int): Matcher<View> {
        return atPositionOnView(position, UNSPECIFIED)
    }

    fun atPositionOnView(position: Int, targetViewId: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var recycler: RecyclerView? = null
            var holder: RecyclerView.ViewHolder? = null
            override fun describeTo(description: Description) {
                Preconditions.checkState(
                    resources != null,
                    "resource should be init by matchesSafely()"
                )
                if (recycler == null) {
                    description.appendText("RecyclerView with " + getResourceName(recyclerId))
                    return
                }
                if (holder == null) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                if (targetViewId == UNSPECIFIED) {
                    description.appendText(
                        String.format(
                            "in RecyclerView (%s) at position %s",
                            getResourceName(recyclerId), position
                        )
                    )
                    return
                }
                description.appendText(
                    String.format(
                        "in RecyclerView (%s) at position %s and with %s",
                        getResourceName(recyclerId),
                        position,
                        getResourceName(targetViewId)
                    )
                )
            }

            private fun getResourceName(id: Int): String {
                return try {
                    "R.id." + resources!!.getResourceEntryName(id)
                } catch (ex: NotFoundException) {
                    String.format("resource id %s - name not found", id)
                }
            }

            public override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                recycler = view.rootView.findViewById(recyclerId)
                if (recycler == null) return false
                holder = recycler!!.findViewHolderForAdapterPosition(position)
                if (holder == null) return false
                return if (targetViewId == UNSPECIFIED) {
                    view === holder!!.itemView
                } else {
                    view === holder!!.itemView.findViewById<View>(targetViewId)
                }
            }
        }
    }

    fun hasItemCount(itemCount: Int): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(
            RecyclerView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has $itemCount items")
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                return view.adapter!!.itemCount == itemCount
            }
        }
    }

    fun hasNotAnItem(textWithinItem: String): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has not item with \"${textWithinItem}\" text")
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val item = (view.adapter!! as ItemAdapter).currentList.find { it -> it.title.equals(textWithinItem) }
                return item == null
            }
        }
    }

    fun hasItemWithRequestedText(text: String, occurrences: Int): Matcher<View> {
        return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("has not ${occurrences} items with ${text} text")
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val count = (view.adapter!! as ItemAdapter).currentList.count { it -> it.title.contains(text) }
                return count == occurrences
            }

        }
    }

    companion object {
        const val UNSPECIFIED = -1
    }
}