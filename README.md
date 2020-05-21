# TigerJython

TigerJython is an educational development environment for Python with enhanced error messages.  It runs on the Java Virtual Machine and already includes [Jython](https://www.jython.org/).  If you have JRE installed, just download the [JAR-file](https://github.com/Tiger-Jython/TigerJython/releases/download/v3.0-ea%2B2/TigerJython3-ea+2.jar) under [**release**](https://github.com/Tiger-Jython/TigerJython/releases/latest).

> This is an early access version that is not yet ready for classroom use.

**Several features are not yet working, including syntax highlighting&mdash;there is no sense in raising issue tickets at the moment, as we are constantly working on the IDE.**



## Plugins

You can write simple plugins for TigerJython.  The plugins are written in Python and run using the internal Jython interpreter.  More information can be found in the [Plugins Guide](PLUGINS.md).



## Publications

The _TigerJython_ IDE has been presented in the paper 
[Tell Me What's Wrong: A Python IDE with Error Messages](https://dl.acm.org/doi/abs/10.1145/3328778.3366920)
as published at the SIGCSE Technical Symposium 2020.  If you use TigerJython for academic work, please cite this paper:
```
@inproceedings{10.1145/3328778.3366920,
  author = {Kohn, Tobias and Manaris, Bill},
  title = {Tell Me What’s Wrong: A Python IDE with Error Messages},
  year = {2020},
  isbn = {9781450367936},
  publisher = {Association for Computing Machinery},
  address = {New York, NY, USA},
  url = {https://doi.org/10.1145/3328778.3366920},
  doi = {10.1145/3328778.3366920},
  booktitle = {Proceedings of the 51st ACM Technical Symposium on Computer Science Education},
  pages = {1054–1060},
  numpages = {7},
  keywords = {ide, compiler error messages, python},
  location = {Portland, OR, USA},
  series = {SIGCSE ’20}
}
```

