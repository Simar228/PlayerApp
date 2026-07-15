package com.example.sound.Presentation.mainScreen.components

data class SortButtonValue(
    var index: Int,
    var isUp: Boolean,
    var isActive: Boolean,
)

fun MutableList<SortButtonValue>.choose(
    index: Int,
): MutableList<SortButtonValue> {
    return this.map {
        if(it.index == index){
            SortButtonValue(
                index = it.index,
                isUp = if(it.isActive) !it.isUp else it.isUp,
                isActive = true
            )
        }else{
            SortButtonValue(
                index = it.index,
                isUp = it.isUp,
                isActive = false
            )
        }
    }.toMutableList()
}