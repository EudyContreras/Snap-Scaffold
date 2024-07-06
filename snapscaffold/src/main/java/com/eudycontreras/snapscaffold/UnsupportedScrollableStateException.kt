package com.eudycontreras.snapscaffold

internal class UnsupportedScrollableStateException: Exception(
    "The supplied scrollable state is not currently supported please use a ScrollState, LazyListState, LazyGridState or LazyStaggeredGridState"
)