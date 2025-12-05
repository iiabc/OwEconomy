package com.hiusers.mc.mimic.core.loader

import com.hiusers.mc.mimic.system.Loadable
import com.hiusers.mc.mimic.system.Register
import taboolib.common.LifeCycle
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.library.reflex.ReflexClass

/**
 * @author iiabc
 * @since 2025/5/7 11:10
 */
@Awake
object LoadableImpl : ClassVisitor(1) {

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.LOAD
    }

    override fun visitStart(clazz: ReflexClass) {
        if (clazz.hasAnnotation(Register::class.java)) {
            LoadableContainer.add(clazz.getInstance() as Loadable)
        }
    }

}