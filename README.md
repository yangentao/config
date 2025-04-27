## config

A struct value format used by config file. include map, array, string, null.

```
val s = """
# comment, all value is string
# use ':' or '=' separate key and value
{
  name : entao
  avg = 99.9         # this is comment
  scores: [1,2,3,]   # last ',' is ignored
  scores2: [1,2,3,,] # => [1,2,3,null]
}
"""

val map = Configs.parse(s).asMap!!
println(map.getString("name"))
println(map.getDouble("avg"))
println(map.getList("scores"))
```

