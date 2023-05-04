package com.baidu.duer.files.util

import android.os.Bundle
import android.os.Parcelable
import kotlin.reflect.KClass

interface ParcelableState : Parcelable

fun <State : ParcelableState> Bundle.putState(state: State) =
    putParcelable(state.javaClass.name, state)

fun <State : ParcelableState> Bundle.getState(stateClass: KClass<State>): State =
    getParcelableSafe(stateClass.java.name)!!

inline fun <reified State : ParcelableState> Bundle.getState() = getState(State::class)
