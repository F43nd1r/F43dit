package com.faendir.om.dsl

class SecureClassLoader(parent: ClassLoader) : ClassLoader(parent) {

    override fun loadClass(name: String?, resolve: Boolean): Class<*>? {
        System.getSecurityManager()?.checkPermission(ClassLoadingPermission(name ?: "*"))
        return super.loadClass(name, resolve)
    }
}