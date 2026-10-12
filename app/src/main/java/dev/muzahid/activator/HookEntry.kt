package dev.muzahid.activator

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class HookEntry : XposedModule() {

    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (param.packageName != "com.muzahid.gps") return
        if (!isLicenseActive(param.classLoader)) return
        hookMuzahidLifecycle()
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != "com.muzahid.gps") return
        if (!isLicenseActive(param.classLoader)) return
        val cl = param.classLoader ?: return
        hookMuzahidGps(cl)
    }

    private fun isLicenseActive(classLoader: ClassLoader?): Boolean {
        return try {
            val context = classLoader?.loadClass("android.app.ActivityThread")
                ?.getDeclaredMethod("currentApplication")?.invoke(null) as? Context ?: return false
            val result = context.contentResolver.call(
                Uri.parse("content://dev.muzahid.activator.XposedService"),
                "license_active", null, null
            )
            result?.getBoolean("license_active", false) == true
        } catch (_: Throwable) {
            false
        }
    }

    private fun hookMuzahidLifecycle() {
        // 1. Toast and auto-inject VIP preference on Activity.onResume
        try {
            val mResume = Activity::class.java.getDeclaredMethod("onResume")
            hook(mResume).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val res = chain.proceed()
                    val act = chain.thisObject as? Activity
                    if (act != null && act.packageName == "com.muzahid.gps") {
                        try {
                            val sp = act.getSharedPreferences("gps", Context.MODE_PRIVATE)
                            sp.edit().putString("get", "VIP_ACTIVE").putBoolean("first_login_shown", true).apply()
                        } catch (_: Throwable) {}
                        try {
                            Toast.makeText(act, "⚡ MUZAHID GPS VIP Activated!", Toast.LENGTH_SHORT).show()
                        } catch (_: Throwable) {}
                    }
                    return res
                }
            })
        } catch (_: Throwable) {}

        // 2. Dismiss any legacy warning dialog on Dialog.show
        try {
            val mShow = Dialog::class.java.getDeclaredMethod("show")
            hook(mShow).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val dialog = chain.thisObject as? Dialog
                    if (dialog != null) {
                        try {
                            val pkg = dialog.context.packageName
                            if (pkg == "com.muzahid.gps" || dialog.ownerActivity?.packageName == "com.muzahid.gps") {
                                dialog.setOnShowListener { d ->
                                    try {
                                        d.dismiss()
                                    } catch (_: Throwable) {}
                                }
                            }
                        } catch (_: Throwable) {}
                    }
                    return chain.proceed()
                }
            })
        } catch (_: Throwable) {}
    }

    private fun hookMuzahidGps(classLoader: ClassLoader) {
        // 1. Hook MainViewModel to make isXposed always emit TRUE (eliminates Xposed Missing Dialog)
        try {
            val vmClass = classLoader.loadClass("com.muzahid.gps.ui.viewmodel.MainViewModel")
            for (m in vmClass.declaredMethods) {
                if (m.name == "updateXposedState") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val res = chain.proceed()
                            val vm = chain.thisObject ?: return res
                            try {
                                for (f in vm.javaClass.declaredFields) {
                                    f.isAccessible = true
                                    val v = f.get(vm) ?: continue
                                    for (sm in v.javaClass.methods) {
                                        if (sm.name == "postValue" || sm.name == "setValue") {
                                            sm.isAccessible = true
                                            sm.invoke(v, java.lang.Boolean.TRUE)
                                        }
                                    }
                                }
                            } catch (_: Throwable) {}
                            return res
                        }
                    })
                } else if (m.name == "isXposed") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val liveData = chain.proceed()
                            if (liveData != null) {
                                try {
                                    for (sm in liveData.javaClass.methods) {
                                        if (sm.name == "postValue" || sm.name == "setValue") {
                                            sm.isAccessible = true
                                            sm.invoke(liveData, java.lang.Boolean.TRUE)
                                        }
                                    }
                                } catch (_: Throwable) {}
                            }
                            return liveData
                        }
                    })
                }
            }
        } catch (_: Throwable) {}

        // 2. Hook Xshare.isHookedSystem to return true
        try {
            val xsClass = classLoader.loadClass("com.muzahid.gps.xposed.Xshare")
            for (m in xsClass.declaredMethods) {
                if (m.name == "isHookedSystem") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? = true
                    })
                }
            }
        } catch (_: Throwable) {}

        // 3. Hook MapActivity.B() dialog getter to auto-dismiss
        try {
            val mapActClass = classLoader.loadClass("com.muzahid.gps.ui.MapActivity")
            for (m in mapActClass.declaredMethods) {
                if (m.name == "B") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val dialog = chain.proceed() as? Dialog
                            try {
                                dialog?.dismiss()
                            } catch (_: Throwable) {}
                            return dialog
                        }
                    })
                }
            }
        } catch (_: Throwable) {}

        // 4. Hook androidx.core.location.location_providor to eliminate Key / License dialog
        try {
            val lpClass = classLoader.loadClass("androidx.core.location.location_providor")
            for (m in lpClass.declaredMethods) {
                when (m.name) {
                    "l" -> {
                        // Completely suppress the "Enter your license key" dialog
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = null
                        })
                    }
                    "geoapi" -> {
                        // Suppress license check & popup flow
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = null
                        })
                    }
                    "i" -> {
                        // Key validation check -> Always TRUE (VIP active)
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = true
                        })
                    }
                    "h" -> {
                        // Date / expiration check -> Always TRUE
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = true
                        })
                    }
                    "g" -> {
                        // Automatic time check -> Always TRUE
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = true
                        })
                    }
                }
            }
        } catch (_: Throwable) {}

        // 5. Hook access_providor signature & integrity check
        try {
            val apClass = classLoader.loadClass("androidx.core.location.access_providor")
            for (m in apClass.declaredMethods) {
                when (m.name) {
                    "g", "x" -> {
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = null
                        })
                    }
                    "sig", "pkg", "blk", "zip", "certs" -> {
                        hook(m).intercept(object : XposedInterface.Hooker {
                            override fun intercept(chain: XposedInterface.Chain): Any? = true
                        })
                    }
                }
            }
        } catch (_: Throwable) {}

        // 6. Hook google.get & google.O0O0o0oo00oo
        try {
            val gClass = classLoader.loadClass("androidx.core.location.google")
            for (m in gClass.declaredMethods) {
                if (m.name == "get" || m.name == "O0O0o0oo00oo") {
                    hook(m).intercept(object : XposedInterface.Hooker {
                        override fun intercept(chain: XposedInterface.Chain): Any? = null
                    })
                }
            }
        } catch (_: Throwable) {}
    }
}
