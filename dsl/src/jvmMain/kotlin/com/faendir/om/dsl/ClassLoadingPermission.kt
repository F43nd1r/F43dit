package com.faendir.om.dsl

import java.security.BasicPermission

class ClassLoadingPermission(name: String) : BasicPermission(name) {
}