package xyz.semio;

import android.util.JsonWriter;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import xyz.semio.script.ScriptInterpreter;

public class Interaction {
  private String _id;

  public Interaction(final String id) {
    this._id = id;
  }
  public String getId() {
    return this._id;
  }

  /**
   * Advances the interaction from a user's given utterance.
   *
   * @param input The user's utterance (from either a text box or speech-to-text, for example)
   * @return A deferred InteractionState or null in the case of failure
   */
  public Promise<InteractionState> next(final String input) {
    Promise<InteractionState> ret = new Promise<InteractionState>();

    // Build JSON req
    JSONObject data = new JSONObject();
    try {
      data.put("input", input);
    } catch(final JSONException e) {
      Log.e(Util.TAG, e.getMessage());
      ret.complete(null);
      return ret;
    }

    // Execute
    HttpRequest req = new HttpRequest(Util.makeApiUrl("interaction/" + _id + "/next"), "POST", data);
    return req.execute().then(new Function<HttpResponse, InteractionState>() {
      @Override
      public InteractionState apply(HttpResponse value) {
        if(value == null) return null;

        JSONObject res = value.getData();
        String utterance = null;
        String script = null;
        Map<String, ScriptInterpreter.Instance> slots = new HashMap<String, ScriptInterpreter.Instance>();
        int next = 0;
        try {
          utterance = res.getString("utterance");
          script = res.has("script") ? res.getString("script") : "";
          JSONArray slotArray = res.has("slots") ? res.getJSONArray("slots") : null;
          if(slotArray != null)
          {
            for(int i = 0; i < slotArray.length(); ++i)
            {
              JSONObject obj = slotArray.getJSONObject(i);
              String valueStr = obj.getString("value");
              ScriptInterpreter.Instance inst = null;
              try {
                if(inst == null) inst = new ScriptInterpreter.Instance(ScriptInterpreter.Instance.Type.INTEGER, Integer.parseInt(valueStr));
              } catch(NumberFormatException e) {}
              try {
                if(inst == null) inst = new ScriptInterpreter.Instance(ScriptInterpreter.Instance.Type.REAL, Double.parseDouble(valueStr));
              } catch(NumberFormatException e) {}

              if(inst == null) inst = new ScriptInterpreter.Instance(ScriptInterpreter.Instance.Type.STRING, valueStr);
              slots.put(obj.getString("slot"), inst);
            }
          }
          next = res.getInt("next");
        } catch(final JSONException e) {
          Log.e(Util.TAG, e.getMessage());
          return null;
        }
        return new InteractionState(utterance, script, next, slots);
      }
    });
  }

  public Promise<Boolean> delete() {
    HttpRequest req = new HttpRequest(Util.makeApiUrl("interaction/" + _id), "DELETE");
    return req.execute().then(new Function<HttpResponse, Boolean>() {
      @Override
      public Boolean apply(HttpResponse response) {
        return response.getCode() == 200;
      }
    });
  }
}
