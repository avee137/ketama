from setuptools import setup, Extension
setup(name="ketama", version="0.1.8",
      zip_safe=True,
      ext_modules=[Extension("ketama", ["ketamamodule.c"],
                             library_dirs=["/usr/local/lib"],
                             include_dirs=["/usr/local/include"],
                             libraries=["ketama"])])
