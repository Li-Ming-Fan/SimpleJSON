# SimpleJSON
Simple JSON parser, loader and saver, JAVA

## Usage

```
// test
public static void test()
{
	HashMap<String, String> dict_test = new HashMap<>();
	dict_test.put("model_tag", "cnn");
	dict_test.put("is_train", "0");
	dict_test.put("num_batch_split", "[12, 20]");
	dict_test.put("mat_shape", "(12, 20)");
	dict_test.put("sub_dict", "{\"a\": 1, \"b\": 2.0, \"c\": \"relu\"}");

	// display
	SimpleJSON.diaplay(dict_test);
	System.out.println("");

	// dump to str
	String dict_str = SimpleJSON.dumpToString(dict_test, 0);
	System.out.println(dict_str);
	System.out.println("");

	// dump to file
	String filepath = "./test.json";
	SimpleJSON.dumpToFile(dict_test, filepath, 4);
	System.out.println("saved");

	// load from file
	HashMap<String, String> dict_load = SimpleJSON.loadFromFile(filepath);
	System.out.println("loaded");

	SimpleJSON.diaplay(dict_load);
	System.out.println("");

	// parse sub_dict
	String sub_dict_str = dict_load.get("sub_dict");
	System.out.println(sub_dict_str);

	HashMap<String, String> sub_dict = SimpleJSON.parseDictAsStringToString(sub_dict_str);
	SimpleJSON.diaplay(sub_dict);
	System.out.println("");

	// parse sub_list
	String sub_list_str = dict_load.get("num_batch_split");
	System.out.println(sub_list_str);

	HashMap<Integer, String> sub_list = SimpleJSON.parseListAsIntegerToString(sub_list_str);
	for (Integer key : sub_list.keySet()) System.out.println("" + key + ": " + sub_list.get(key));
	System.out.println("");

}
```

Running the above function will lead to the result:

```

"sub_dict": {"a": 1, "b": 2.0, "c": "relu"}
"num_batch_split": [12, 20]
"model_tag": "cnn"
"mat_shape": (12, 20)
"is_train": 0

{"sub_dict": {"a": 1, "b": 2.0, "c": "relu"}, "num_batch_split": [12, 20], "model_tag": "cnn", "mat_shape": (12, 20), "is_train": 0}

saved
loaded
"sub_dict": {"a": 1, "b": 2.0, "c": "relu"}
"num_batch_split": [12, 20]
"model_tag": "cnn"
"mat_shape": (12, 20)
"is_train": 0

{"a": 1, "b": 2.0, "c": "relu"}
"a": 1
"b": 2.0
"c": "relu"

[12, 20]
0: 12
1: 20

```

and the lines in the file ./test.json

```
{
    "sub_dict": {"a": 1, "b": 2.0, "c": "relu"},
    "num_batch_split": [12, 20],
    "model_tag": "cnn",
    "mat_shape": (12, 20),
    "is_train": 0
}

```
