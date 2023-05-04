package com.baidu.duer.files.compat

import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import java.util.*

fun Calendar.toInstantCompat(): Instant = DateTimeUtils.toInstant(this)
