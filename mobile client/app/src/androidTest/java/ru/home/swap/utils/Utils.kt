package ru.home.swap.utils

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import ru.home.swap.R


object Utils {

    fun atPositionByTitle(position: Int, itemMatcher: Matcher<View?>, @IdRes title: Int = R.id.offer_title): Matcher<View?>? {
        checkNotNull(itemMatcher)
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder = view.findViewHolderForAdapterPosition(position)
                    ?: // has no item on such position
                    return false
                val item = matchByTitle(viewHolder.itemView, title)
                return itemMatcher.matches(item)
            }
        }
    }

    fun matchByTitle(root: View, @IdRes title: Int): View {
        return root.findViewById(title)
    }
}