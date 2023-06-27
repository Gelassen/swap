package ru.home.swap.utils

import ru.home.swap.core.model.Service

class ServiceIdsComparator : Comparator<Service> {
    override fun compare(left: Service, right: Service): Int {
        // two items in list with the same ids is illegal state condition,
        // but it would be better to gracefully cover such case instead of
        // throwing an exception
        return if (left.id > right.id || left.id == right.id) -1 else 1
    }
}