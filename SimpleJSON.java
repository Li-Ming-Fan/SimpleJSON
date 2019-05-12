package SimpleJSON;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/*
 * created by Ming-Fan Li, 2019-05-11.
 */
public class SimpleJSON
{
	// transform, dump
	public static String trimQuotationMasks(String str)
	{
		if (str.startsWith("\""))
		{
			return str.substring(1, str.length() - 1);
		}
		else
		{
			return str;
		}
	}
	public static String wrapWithQuotationMasks(String str)
	{
		if (str.matches("^[0-9]*"))   // int, float
		{
			return str;
		}
		else if (str.startsWith("[") || str.startsWith("(") || str.startsWith("{")) // list, tuple, dict
		{
			return str;
		}
		else
		{
			return "\""+str+"\"";
		}
	}
	//
	public static String dumpToString(HashMap<String, String> dict, Integer indent)
	{
		String indent_str = "";
		for (int idx = 0; idx < indent; idx ++) indent_str += " ";
		//
		List<String> keys = new ArrayList<String>();
		keys.addAll(dict.keySet());
		Integer num_attr = keys.size();
		//
		StringBuilder sb = new StringBuilder("{");
		if (indent > 0)
		{
			sb.append("\n");
			//
			String key = keys.get(0);
			// System.out.println(key);
			sb.append(indent_str);
			sb.append(wrapWithQuotationMasks(key));
			sb.append(": ");
			sb.append(wrapWithQuotationMasks(dict.get(key)));
			//
			for (int idx = 1; idx < num_attr; idx++)
			{
				key = keys.get(idx);
				// System.out.println(key);
				//
				sb.append(",\n");
				sb.append(indent_str);
				sb.append(wrapWithQuotationMasks(key));
				sb.append(": ");
				sb.append(wrapWithQuotationMasks(dict.get(key)));
	        }
			sb.append("\n");			
		}
		else // (indent <= 0)
		{
			String key = keys.get(0);
			// System.out.println(key);
			sb.append(wrapWithQuotationMasks(key));
			sb.append(": ");
			sb.append(wrapWithQuotationMasks(dict.get(key)));
			//
			for (int idx = 1; idx < num_attr; idx++)
			{
				key = keys.get(idx);
				// System.out.println(key);
				sb.append(", ");
				sb.append(wrapWithQuotationMasks(key));
				sb.append(": ");
				sb.append(wrapWithQuotationMasks(dict.get(key)));
	        }
		}
		sb.append("}");
		return sb.toString();
	}
	public static void dumpToFile(HashMap<String, String> dict, String filepath, Integer indent)
	{
		File file=new File(filepath);
		if (file.exists())
		{
			file.delete();
		}
		try
		{
			file.createNewFile();
		}
		catch (IOException ioe) {  }
		//
		try
		{
			BufferedWriter bw=new BufferedWriter(new FileWriter(filepath));
			//
			String str_dict = SimpleJSON.dumpToString(dict, indent);
			bw.write(str_dict);
			bw.newLine();
			bw.flush();
			bw.close();	
		}
		catch (IOException ioe)
		{	} // try ListDateRest.txt

	}
	//
	
	// load json file
	public static HashMap<String, String> loadFromFile(String filepath)
	{
		StringBuilder str_all = new StringBuilder();
		//
		try
		{
			BufferedReader br=new BufferedReader(new FileReader(filepath));
			//
			String line = br.readLine();
			while (line != null)
			{
				str_all.append(line.trim());
				line = br.readLine();
			}
			//
			br.close();	
		}
		catch (IOException ioe)
		{
		} // try SystemRecord.tx
		//
		HashMap<String, String> dict_load = SimpleJSON.parseDictAsStringToString(str_all.toString());
		return dict_load;		
	}
	//
	
	// display
	public static void diaplay(HashMap<String, String> dict_map)
	{
		for (String key : dict_map.keySet())
		{
			String key_wrapped = SimpleJSON.wrapWithQuotationMasks(key);
			String value_wrapped = SimpleJSON.wrapWithQuotationMasks(dict_map.get(key));
			System.out.println(key_wrapped + ": " + value_wrapped);
		}
	}
	//
	
	//
	// dict, could be nested
	public static HashMap<String, String> parseDictAsStringToString(String str)
	{
		str = str.trim();
		
		HashMap<Integer, Integer> posi_type = SimpleJSON.getBraketPositions(str);
		HashMap<Integer, Integer> outer_pairs = SimpleJSON.getOuterBrakets(posi_type);
		
		HashMap<String, String> replacement = new HashMap<>();
		String str_replaced =  SimpleJSON.replaceOuterItems(str, outer_pairs, replacement);
		
		HashMap<String, String> result = new HashMap<>();
		result = SimpleJSON.parseDictNotNested(str_replaced);
		result = SimpleJSON.replaceDictRawParsed(result, replacement);

		return result;
	}	
	//
	// list, could be nested
	public static HashMap<Integer, String> parseListAsIntegerToString(String str)
	{
		str = str.trim();
		
		HashMap<Integer, Integer> posi_type = SimpleJSON.getBraketPositions(str);
		HashMap<Integer, Integer> outer_pairs = SimpleJSON.getOuterBrakets(posi_type);
		
		HashMap<String, String> replacement = new HashMap<>();
		String str_replaced =  SimpleJSON.replaceOuterItems(str, outer_pairs, replacement);
		
		HashMap<Integer, String> result = new HashMap<>();
		result = SimpleJSON.parseListNotNested(str_replaced);
		result = SimpleJSON.replaceListTupleRawParsed(result, replacement);

		return result;
	}	
	//
	// tuple, could be nested
	public static HashMap<Integer, String> parseTupleAsIntegerToString(String str)
	{
		str = str.trim();
		
		HashMap<Integer, Integer> posi_type = SimpleJSON.getBraketPositions(str);
		HashMap<Integer, Integer> outer_pairs = SimpleJSON.getOuterBrakets(posi_type);
		
		HashMap<String, String> replacement = new HashMap<>();
		String str_replaced =  SimpleJSON.replaceOuterItems(str, outer_pairs, replacement);
		
		HashMap<Integer, String> result = new HashMap<>();
		result = SimpleJSON.parseTupleNotNested(str_replaced);
		result = SimpleJSON.replaceListTupleRawParsed(result, replacement);

		return result;
	}	
	//
	
	//
	// dict, not nested, basic type elements: int, float, str
	public static HashMap<String, String> parseDictNotNested(String str_not_nested)
	{
		// replace the root "{" and "}"
		str_not_nested = str_not_nested.replace("{", "").replace("}", "");
		// split
		String [] str_arr = str_not_nested.split(",");
		// parse
		HashMap<String, String> result = new HashMap<>();
		for (String item : str_arr)
		{
			String [] item_kv = item.split(":");
			String key = SimpleJSON.trimQuotationMasks(item_kv[0].trim());
			String value = SimpleJSON.trimQuotationMasks(item_kv[1].trim());
			result.put(key, value);
		}
		return result;	
	}
	//
	// list, not nested, basic type elements: int, float, str
	public static HashMap<Integer, String> parseListNotNested(String str_not_nested)
	{
		// replace the root "[" and "]"
		str_not_nested = str_not_nested.replace("[", "").replace("]", "");
		// split
		String [] str_arr = str_not_nested.split(",");
		Integer num_items = str_arr.length;
		// parse
		HashMap<Integer, String> result = new HashMap<>();
		for (Integer idx = 0; idx < num_items; idx++)
		{
			result.put(idx, SimpleJSON.trimQuotationMasks(str_arr[idx].trim()));
		}
		return result;	
	}
	//
	// tuple, not nested, basic type elements: int, float, str
	public static HashMap<Integer, String> parseTupleNotNested(String str_not_nested)
	{
		// replace the root "(" and ")"
		str_not_nested = str_not_nested.replace("(", "").replace(")", "");
		// split
		String [] str_arr = str_not_nested.split(",");
		Integer num_items = str_arr.length;
		// parse
		HashMap<Integer, String> result = new HashMap<>();
		for (Integer idx = 0; idx < num_items; idx++)
		{
			result.put(idx, SimpleJSON.trimQuotationMasks(str_arr[idx].trim()));
		}
		return result;	
	}
	//
	
	// parse procedure for nested dict, list, tuple,
	public static List<Integer> findPositionsOfSubstring(String parent, String child)
    {
		int len_child = child.length();
		
        List<Integer> positions = new ArrayList<>();
        int index = 0;
        while( ( index = parent.indexOf(child, index) ) != -1 )
        {
        	positions.add(index);
            index = index + len_child;
        }
        return positions;
    }
	//
	private static HashMap<Integer, Integer> getBraketPositions(String str)
	{
		// not incorporate the root pair
		Integer len_str = str.length();
		String str_trim = "L" + str.substring(1, len_str-1) + "R";
		
		List<Integer> posi_arc_left = SimpleJSON.findPositionsOfSubstring(str_trim, "(");
		List<Integer> posi_square_left = SimpleJSON.findPositionsOfSubstring(str_trim, "[");
		List<Integer> posi_curly_left = SimpleJSON.findPositionsOfSubstring(str_trim, "{");
		
		List<Integer> posi_arc_right = SimpleJSON.findPositionsOfSubstring(str_trim, ")");
		List<Integer> posi_square_right = SimpleJSON.findPositionsOfSubstring(str_trim, "]");
		List<Integer> posi_curly_right = SimpleJSON.findPositionsOfSubstring(str_trim, "}");
		
		//
		HashMap<Integer, Integer> posi_type = new HashMap<>();
		for (Integer posi : posi_arc_left) posi_type.put(posi, 0);
		for (Integer posi : posi_square_left) posi_type.put(posi, 0);
		for (Integer posi : posi_curly_left) posi_type.put(posi, 0);
		
		for (Integer posi : posi_arc_right) posi_type.put(posi, 1);
		for (Integer posi : posi_square_right) posi_type.put(posi, 1);
		for (Integer posi : posi_curly_right) posi_type.put(posi, 1);
		
		return posi_type;		
	}
	private static HashMap<Integer, Integer> getOuterBrakets(HashMap<Integer, Integer> posi_type)
	{
		List<Integer> positions = new ArrayList<>();
		positions.addAll(posi_type.keySet());	
		Integer num_posi = positions.size();
		
		Collections.sort(positions); //默认排序(从小到大)    // Collections.reverse(list_int_str_basic);//倒叙(从大到小)
		
		HashMap<Integer, Integer> result_pairs = new HashMap<>();
		
		Stack<Integer> stack_posi = new Stack<Integer>();
		for (int idx = 0; idx < num_posi; idx++)
		{
			Integer posi = positions.get(idx);
			Integer type = posi_type.get(posi);
			if (type == 0)
			{
				stack_posi.push(posi);
			}
			else
			{
				Integer posi_left = stack_posi.pop();
				//
				// only the outer bracket
				if (stack_posi.isEmpty())
				{
					result_pairs.put(posi_left, posi);					
				}
			}			
		}
		
		return result_pairs;
	}
	private static String replaceOuterItems(String str, HashMap<Integer, Integer> outer_pairs, HashMap<String, String> replacement)
	{
		StringBuilder result = new StringBuilder();
		Integer posi_last = 0;
		Integer count = 0;
		String RepTemp = "replaced_temp";
		
		List<Integer> posi_left_all = new ArrayList<>();
		posi_left_all.addAll(outer_pairs.keySet());
		Integer num_pairs = posi_left_all.size();
		
		Collections.sort(posi_left_all); //默认排序(从小到大)    // Collections.reverse(list_int_str_basic);//倒叙(从大到小)
		
		for (int idx = 0; idx < num_pairs; idx++)
		{
			Integer posi_left = posi_left_all.get(idx);
			Integer posi_right_m1 = outer_pairs.get(posi_left) + 1;
			
			result.append(str.substring(posi_last, posi_left));
			
			String str_rep = RepTemp.replace("temp", "" + count);
			result.append(str_rep);
			
			replacement.put(str_rep, str.substring(posi_left, posi_right_m1));
			
			posi_last = posi_right_m1;
			count++;
		}
		//
		result.append(str.substring(posi_last, str.length()));
		//		
		return result.toString();	
	}
	//
	private static HashMap<String, String> replaceDictRawParsed(HashMap<String, String> result_raw, HashMap<String, String> replacement)
	{
		HashMap<String, String> result = new HashMap<>();
		
		for (String key : result_raw.keySet())
		{
			String value = result_raw.get(key);
			//
			if (replacement.size() == 0)
			{
				result.put(key, value);
				continue;				
			}
			//
			int flag_replaced = 0;
			for (String str_rep : replacement.keySet())
			{
				if (value.contains(str_rep))
				{
					value = value.replaceAll(str_rep, replacement.get(str_rep));
					result.put(key, value);
					flag_replaced = 1;
					replacement.remove(str_rep);
					break;
				}
			}
			//
			if (flag_replaced == 0)
			{
				result.put(key, value);				
			}
		}
		
		return result;	
	}
	private static HashMap<Integer, String> replaceListTupleRawParsed(HashMap<Integer, String> result_raw, HashMap<String, String> replacement)
	{
		HashMap<Integer, String> result = new HashMap<>();
		
		for (Integer key : result_raw.keySet())
		{
			String value = result_raw.get(key);
			//
			if (replacement.size() == 0)
			{
				result.put(key, value);
				continue;
			}
			//
			int flag_replaced = 0;
			for (String str_rep : replacement.keySet())
			{
				if (value.contains(str_rep))
				{
					value = value.replaceAll(str_rep, replacement.get(str_rep));
					result.put(key, value);
					flag_replaced = 1;
					replacement.remove(str_rep);
					break;
				}
			}
			//
			if (flag_replaced == 0)
			{
				result.put(key, value);				
			}
		}
		
		return result;	
	}
	//
	
	// test
	public static void test()
	{
		HashMap<String, String> dict_test = new HashMap<>();
		dict_test.put("model_tag", "cnn");
		dict_test.put("is_train", "0");
		dict_test.put("num_batch_split", "[12, 20]");
		dict_test.put("mat_shape", "(12, 20)");
		
		SimpleJSON.diaplay(dict_test);
		System.out.println("");
		
		//
		String filepath = "./test.json";
		SimpleJSON.dumpToFile(dict_test, filepath, 4);
		System.out.println("saved");
		
		//
		HashMap<String, String> dict_load = SimpleJSON.loadFromFile(filepath);
		System.out.println("loaded");
		
		SimpleJSON.diaplay(dict_load);
		System.out.println("");
		
	}
	//
	public static void main(String args[])
	{
		test();		
	}
	//
	
}
