package tigerjython.jython

import org.python.core._

import math._
import javax.sound.sampled.{AudioFormat, AudioSystem}
import javax.sound.midi.MidiSystem

/**
 * This module provides support for playing simple audio (sine waves and MIDI).
 *
 * @author Tobias Kohn
 */
object SoundSupport {

  /**
   * The sample rate determines the quality.
   */
  var sampleRate = 16000;	// 16 kHz

  /**
   * Set this value to influence the attack/decay length
   * (depending on the sampleRate).
   */
  private val decayLen = 320;

  /**
   * Set this value to a maximum duration a sound can play.
   */
  private val maxDuration = 600000;

  /**
   * The duration used in case none is given.
   */
  private val defaultDuration = 250;

  /**
   * The volume used in case none is given.
   */
  private val defaultVolume = 50

  /**
   * Maps the different notes to midi notes, using the notation of Helmholtz
   * as well as the scientific pitch notation.
   */
  private val noteData = Map(
    "-"   -> (-1,   0.00),

    "C0"  -> (12,  16.35),  "C,,"  -> (12,  16.35),
    "C#0" -> (13,  17.32),  "C,,#" -> (13,  17.32),  "C#,," -> (13,  17.32),
    "D0"  -> (14,  18.35),  "D,,"  -> (14,  18.35),
    "D#0" -> (15,  19.44),  "D,,#" -> (15,  19.44),  "D#,," -> (15,  19.44),
    "E0"  -> (16,  20.60),  "E,,"  -> (16,  20.60),
    "F0"  -> (17,  21.83),  "F,,"  -> (17,  21.83),
    "F#0" -> (18,  23.12),  "F,,#" -> (18,  23.12),  "F#,," -> (18,  23.12),
    "G0"  -> (19,  24.50),  "G,,"  -> (19,  24.50),
    "G#0" -> (20,  25.96),  "G,,#" -> (20,  25.96),  "G#,," -> (20,  25.96),
    "A0"  -> (21,  27.50),  "A,,"  -> (21,  27.50),
    "A#0" -> (22,  29.13),  "A,,#" -> (22,  29.13),  "A#,," -> (22,  29.13),
    "B0"  -> (23,  30.87),  "B,,"  -> (23,  30.87),  "H,,"  -> (23,  30.87),

    "C1"  -> (24,  32.70),  "C,"  -> (24,  32.70),
    "C#1" -> (25,  34.65),  "C,#" -> (25,  34.65),  "C#," -> (25,  34.65),
    "D1"  -> (26,  36.71),  "D,"  -> (26,  36.71),
    "D#1" -> (27,  38.89),  "D,#" -> (27,  38.89),  "D#," -> (27,  38.89),
    "E1"  -> (28,  41.20),  "E,"  -> (28,  41.20),
    "F1"  -> (29,  43.65),  "F,"  -> (29,  43.65),
    "F#1" -> (30,  46.25),  "F,#" -> (30,  46.25),  "F#," -> (30,  46.25),
    "G1"  -> (31,  49.00),  "G,"  -> (31,  49.00),
    "G#1" -> (32,  51.91),  "G,#" -> (32,  51.91),  "G#," -> (32,  51.91),
    "A1"  -> (33,  55.00),  "A,"  -> (33,  55.00),
    "A#1" -> (34,  58.27),  "A,#" -> (34,  58.27),  "A#," -> (34,  58.27),
    "B1"  -> (35,  61.74),  "B,"  -> (35,  61.74),  "H,"  -> (35,  61.74),

    "C2"  -> (36,  65.41),  "C"  -> (36,  65.41),
    "C#2" -> (37,  69.30),  "C#" -> (37,  69.30),
    "D2"  -> (38,  73.42),  "D"  -> (38,  73.42),
    "D#2" -> (39,  77.78),  "D#" -> (39,  77.78),
    "E2"  -> (40,  82.41),  "E"  -> (40,  82.41),
    "F2"  -> (41,  87.31),  "F"  -> (41,  87.31),
    "F#2" -> (42,  92.50),  "F#" -> (42,  92.50),
    "G2"  -> (43,  98.00),  "G"  -> (43,  98.00),
    "G#2" -> (44, 103.83),  "G#" -> (44, 103.83),
    "A2"  -> (45, 110.00),  "A"  -> (45, 110.00),
    "A#2" -> (46, 116.54),  "A#" -> (46, 116.54),
    "B2"  -> (47, 123.47),  "B"  -> (47, 123.47),  "H"  -> (47, 123.47),

    "C3"  -> (48, 130.81),  "c"  -> (48, 130.81),
    "C#3" -> (49, 138.59),  "c#" -> (49, 138.59),
    "D3"  -> (50, 146.83),  "d"  -> (50, 146.83),
    "D#3" -> (51, 155.56),  "d#" -> (51, 155.56),
    "E3"  -> (52, 164.81),  "e"  -> (52, 164.81),
    "F3"  -> (53, 174.61),  "f"  -> (53, 174.61),
    "F#3" -> (54, 185.00),  "f#" -> (54, 185.00),
    "G3"  -> (55, 196.00),  "g"  -> (55, 196.00),
    "G#3" -> (56, 207.65),  "g#" -> (56, 207.65),
    "A3"  -> (57, 220.00),  "a"  -> (57, 220.00),
    "A#3" -> (58, 233.08),  "a#" -> (58, 233.08),
    "B3"  -> (59, 246.94),  "b"  -> (59, 246.94),  "h"  -> (59, 246.94),

    "C4"  -> (60, 261.63),  "c'"  -> (60, 261.63),
    "C#4" -> (61, 277.18),  "c'#" -> (61, 277.18),  "c#'" -> (61, 277.18),
    "D4"  -> (62, 293.66),  "d'"  -> (62, 293.66),
    "D#4" -> (63, 311.13),  "d'#" -> (63, 311.13),  "d#'" -> (63, 311.13),
    "E4"  -> (64, 329.63),  "e'"  -> (64, 329.63),
    "F4"  -> (65, 349.23),  "f'"  -> (65, 349.23),
    "F#4" -> (66, 370.00),  "f'#" -> (66, 370.00),  "f#'" -> (66, 370.00),
    "G4"  -> (67, 392.00),  "g'"  -> (67, 392.00),
    "G#4" -> (68, 415.30),  "g'#" -> (68, 415.30),  "g#'" -> (68, 415.30),
    "A4"  -> (69, 440.00),  "a'"  -> (69, 440.00),
    "A#4" -> (70, 466.16),  "a'#" -> (70, 466.16),  "a#'" -> (70, 466.16),
    "B4"  -> (71, 493.88),  "b'"  -> (71, 493.88),  "h'"  -> (71, 493.88),

    "C5"  -> (72, 523.25),  "c''"  -> (72, 523.25),
    "C#5" -> (73, 554.37),  "c''#" -> (73, 554.37),  "c#''" -> (73, 554.37),
    "D5"  -> (74, 587.33),  "d''"  -> (74, 587.33),
    "D#5" -> (75, 622.25),  "d''#" -> (75, 622.25),  "d#''" -> (75, 622.25),
    "E5"  -> (76, 659.26),  "e''"  -> (76, 659.26),
    "F5"  -> (77, 698.45),  "f''"  -> (77, 698.45),
    "F#5" -> (78, 740.00),  "f''#" -> (78, 740.00),  "f#''" -> (78, 740.00),
    "G5"  -> (79, 784.00),  "g''"  -> (79, 784.00),
    "G#5" -> (80, 830.61),  "g''#" -> (80, 830.61),  "g#''" -> (80, 830.61),
    "A5"  -> (81, 880.00),  "a''"  -> (81, 880.00),
    "A#5" -> (82, 932.33),  "a''#" -> (82, 932.33),  "a#''" -> (82, 932.33),
    "B5"  -> (83, 987.77),  "b''"  -> (83, 987.77),  "h''"  -> (83, 987.77),

    "C6"  -> (84, 1046.50),  "c'''"  -> (84, 1046.50),
    "C#6" -> (85, 1108.73),  "c'''#" -> (85, 1108.73),  "c#'''" -> (85, 1108.73),
    "D6"  -> (86, 1174.66),  "d'''"  -> (86, 1174.66),
    "D#6" -> (87, 1244.51),  "d'''#" -> (87, 1244.51),  "d#'''" -> (87, 1244.51),
    "E6"  -> (88, 1318.51),  "e'''"  -> (88, 1318.51),
    "F6"  -> (89, 1396.91),  "f'''"  -> (89, 1396.91),
    "F#6" -> (90, 1480.00),  "f'''#" -> (90, 1480.00),  "f#'''" -> (90, 1480.00),
    "G6"  -> (91, 1567.98),  "g'''"  -> (91, 1567.98),
    "G#6" -> (92, 1661.22),  "g'''#" -> (92, 1661.22),  "g#'''" -> (92, 1661.22),
    "A6"  -> (93, 1760.00),  "a'''"  -> (93, 1760.00),
    "A#6" -> (94, 1864.66),  "a'''#" -> (94, 1864.66),  "a#'''" -> (94, 1864.66),
    "B6"  -> (95, 1975.53),  "b'''"  -> (95, 1975.53),  "h'''"  -> (95, 1975.53)
  )

  /**
   * A small list of some instruments made available.
   */
  private val instruments = Map(
    "piano" -> 0,
    "glockenspiel" -> 9,
    "musicbox" -> 10,
    "xylophone" -> 13,
    "organ" -> 19,
    "guitar" -> 25,
    "violin" -> 40,
    "cello" -> 42,
    "harp" -> 46,
    "strings" -> 49,
    "tremolo" -> 44,
    "tremolostrings" -> 44,
    "pizzicato" -> 45,
    "pizzicatostrings" -> 45,
    "trumpet" -> 56,
    "sax" -> 65,
    "oboe" -> 68,
    "clarinet" -> 71,
    "flute" -> 73,
    "panflute" -> 75,
    "banjo" -> 105
  )

  /**
   * A selection of MIDI sound effects.
   * (instrument, note, duration)
   */
  private val soundEffects = Map(
    "seashore" -> (122, 50, 4500),
    "seashore1" -> (122, 35, 4500),
    "seashore2" -> (122, 42, 4500),
    "seashore3" -> (122, 50, 4500),
    "seashore4" -> (122, 60, 4500),
    "bird" -> (123, 60, 1000),
    "bird1" -> (123, 40, 1000),
    "bird2" -> (123, 50, 1000),
    "bird3" -> (123, 60, 1000),
    "bird4" -> (123, 70, 1000),
    "phone" -> (124, 60, 700),
    "telephone" -> (124, 60, 700),
    "gunshot" -> (127, 60, 600),
    "gun" -> (127, 60, 600),
    "shot" -> (127, 60, 600),
  )

  private abstract class Tone {
    def duration: Int
    def volume: Int
  }
  private case class FreqNote(frequency: Double, duration: Int, volume: Int) extends Tone
  private case class MidiNote(notes: Array[Int], duration: Int, volume: Int, instrument: Int) extends Tone
  private case class SoundEffect(note: Int, duration: Int, volume: Int, instrument: Int) extends Tone

  private def _isLetter(c: Char): Boolean =
    "ABCDEFGHabcdefgh".contains(c)

  private def _isModifier(c: Char): Boolean =
    "#',0123456".contains(c)

  private def stringToNoteValues(s: String): Array[Int] = {
    val result = collection.mutable.ArrayBuffer[Int]()
    var i = 0
    while (i < s.length && !_isLetter(s(i)))
      i += 1
    while (i < s.length) {
      val start = i
      i += 1
      while (i < s.length && _isModifier(s(i)))
        i += 1
      noteData.get(s.substring(start, i)) match {
        case Some((note, _)) =>
          result += note
        case None =>
          throw Py.ValueError("not a valid note: " + s.substring(start, i))
      }
      while (i < s.length && !_isLetter(s(i)))
        i += 1
    }
    result.toArray
  }

  private def parseArgument(args: Array[PyObject], keywords: Array[String]): Tone = {
    var duration = defaultDuration
    var volume = defaultVolume
    var instrument: Int = -1
    val argDelta = args.length - keywords.length
    if (keywords.nonEmpty) {
      for ((keyword, value) <- keywords zip args.drop(argDelta))
        keyword match {
          case "duration" =>
            value match {
              case _: PyInteger | _: PyFloat =>
                duration = value.asInt() min maxDuration
              case _ =>
                throw Py.TypeError("duration must be int value")
            }
          case "volume" =>
            value match {
              case _: PyInteger | _: PyFloat =>
                volume = value.asInt()
              case _ =>
                throw Py.TypeError("volume must be int value")
            }
          case "instrument" =>
            value match {
              case _: PyString | _: PyUnicode =>
                instruments.get(value.asString.toLowerCase) match {
                  case Some(instr) =>
                    instrument = instr
                  case None =>
                    throw Py.TypeError("unknown instrument: " + value.asString())
                }
              case i: PyInteger =>
                instrument = i.asInt()
              case _ =>
                throw Py.TypeError("instrument must be string value")
            }
          case x =>
            throw Py.TypeError("extra keyword argument: " + x)
        }
    }
    if (argDelta > 2)
      throw Py.TypeError("too many arguments")
    else if (argDelta == 0)
      throw Py.TypeError("too few arguments")
    if (argDelta == 2) {
      if (keywords.contains("duration"))
        throw Py.TypeError("double argument: 'duration'")
      args(1) match {
        case value @ (_: PyInteger | _: PyFloat) =>
          duration = value.asInt()
        case _ =>
          throw Py.TypeError("duration must be int value")
      }
    }
    args(0) match {
      case i: PyInteger =>
        val value = i.asInt()
        if (value < 128 || instrument >= 0) {
          MidiNote(Array(value), duration, volume, instrument max 0)
        } else
          FreqNote(value.toDouble, duration, volume)
      case f: PyFloat =>
        val value = f.asDouble()
        FreqNote(value, duration, volume)
      case s: PyString =>
        val value = s.asString()
        soundEffects.get(value.toLowerCase) match {
          case Some((instr, note, dur)) =>
            SoundEffect(note, dur, volume, instr)
          case None =>
            MidiNote(stringToNoteValues(value), duration, volume, instrument max 0)
        }
      case t: PyTuple if !t.isEmpty =>
        val pyValues = t.getArray
        if (pyValues.length == 2 && keywords.length == 0)
        if (pyValues.forall(_.isInstanceOf[PyInteger])) {
          val values = pyValues.map(_.asInt())
          if (values.forall(_ < 128) || instrument >= 0)
            return MidiNote(values, duration, volume, instrument max 0)
        }
        throw Py.TypeError("invalid argument")
      case _ =>
        throw Py.TypeError("invalid argument")
    }
  }

  private def playMidiSound(sound: MidiNote): Unit = {
    val vol = sound.volume * 127 / 100
    val synthesizer = MidiSystem.getSynthesizer
    synthesizer.open()
    val instruments = synthesizer.getAvailableInstruments
    val channel = synthesizer.getChannels()(0)
    if (0 <= sound.instrument && sound.instrument < instruments.length) {
      val instr = instruments(sound.instrument)
      synthesizer.loadInstrument(instr)
      channel.programChange(instr.getPatch.getProgram)
    }
    val latency = ((synthesizer.getLatency+500) / 1000).toInt
    for (note <- sound.notes)
      channel.noteOn(note, vol)
    Thread.sleep(sound.duration + latency)
    for (note <- sound.notes)
      channel.noteOff(note)
    // We have to wait for a short while for the midi-synthesizer to actually play
    // the notes before shutting it down.
    Thread.sleep(2 * latency)
    channel.allNotesOff()
    synthesizer.close()
  }

  private def playSoundEffect(effect: SoundEffect): Unit = {
    val vol = effect.volume * 127 / 100
    val synthesizer = MidiSystem.getSynthesizer
    synthesizer.open()
    val instruments = synthesizer.getAvailableInstruments
    val latency = ((synthesizer.getLatency+500) / 1000).toInt
    val channel = synthesizer.getChannels()(1)
    val index = effect.instrument
    if (0 <= index && index < instruments.length) {
      val instr = instruments(index)
      synthesizer.loadInstrument(instr)
      channel.programChange(instr.getPatch.getProgram)
      channel.noteOn(effect.note, vol)
      Thread.sleep(min(effect.duration + latency, maxDuration * 4))
      channel.noteOff(effect.note)
    }
    Thread.sleep(2 * latency)
    channel.allNotesOff()
    synthesizer.close()
  }

  private def playWAVSound(sound: FreqNote): Unit = {
    val af = new AudioFormat(sampleRate, 8, 1, true, true)
    val line = AudioSystem.getSourceDataLine(af)
    line.open(af, sampleRate)
    line.start()
    val sineWave = createSineWave(sound.frequency, sound.duration, sound.volume)
    line.write(sineWave, 0, sineWave.length)
    line.drain()
    line.stop()
    line.close()
  }

  /**
   * Create an array of byte holding the wav-data to play.
   *
   * @param frequency  The frequency of the sine wave to generate.
   * @param duration   The duration of the tone in milliseconds.
   * @param volume     The volume as a value between 0 and 127.
   *
   * @return An array of byte with the wav-data to be played.
   */
  private def createSineWave(frequency: Double, duration: Int, volume: Double): Array[Byte] = {
    val dataPoints = duration * sampleRate / 1000
    val cfreq = 2.0 * Pi * frequency / sampleRate.toDouble
    val vol = min(abs(volume), 127.0)
    val result = new Array[Byte](dataPoints)
    for (x <- 0 until dataPoints)
      if (x < decayLen)
        result(x) = (sin(x * cfreq) * vol * x / decayLen).toByte
      else if (x > dataPoints - decayLen)
        result(x) = (sin(x * cfreq) * vol * (dataPoints - x) / decayLen).toByte
      else
        result(x) = (sin(x * cfreq) * vol).toByte
    result
  }

  def playTone(args: Array[PyObject], keywords: Array[String]): Unit =
    parseArgument(args, keywords) match {
      case sound: MidiNote =>
        playMidiSound(sound)
      case effect: SoundEffect =>
        playSoundEffect(effect)
      case sound: FreqNote =>
        playWAVSound(sound)
    }

  def startTone(args: Array[PyObject], keywords: Array[String]): Unit =
    parseArgument(args, keywords) match {
      case sound: MidiNote =>
        val t = new Thread(() => { playMidiSound(sound) })
        t.setDaemon(true)
        t.start()
      case effect: SoundEffect =>
        val t = new Thread(() => { playSoundEffect(effect) })
        t.setDaemon(true)
        t.start()
      case sound: FreqNote =>
        val t = new Thread(() => { playWAVSound(sound) })
        t.setDaemon(true)
        t.start()
    }
}
