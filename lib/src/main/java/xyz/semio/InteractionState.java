package xyz.semio;

import java.util.Map;

import xyz.semio.script.ScriptInterpreter;

public class InteractionState {
  private String _utterance;
  private int _nextNode;
  private String _script;
  private Map<String, ScriptInterpreter.Instance> _slots;

  InteractionState(final String utterance, final String script, final int next, final Map<String, ScriptInterpreter.Instance> slots) {
    this._utterance = utterance;
    this._script = script;
    this._nextNode = next;
    this._slots = slots;
  }

  public int getNextNode() {
    return this._nextNode;
  }
  public String getUtterance() {
    return this._utterance;
  }
  public String getScript() {
    return this._script;
  }
  public Map<String, ScriptInterpreter.Instance> getSlots() { return this._slots; }
}
