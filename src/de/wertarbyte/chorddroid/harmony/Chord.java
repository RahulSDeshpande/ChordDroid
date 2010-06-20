/*
 * Created on 18.06.2010
 *
 */
package de.wertarbyte.chorddroid.harmony;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chord implements Polynote<Chord> {
	
	private Note root;
	private SortedSet<ChordComponent> comps;
	private Note base;
	
	private final static Pattern CHORD_RE = Pattern.compile("^([a-g][b]?)(|m|aug|dim|5|sus[24])(6|(maj)?7)?(/([a-g][b]?))?$");
	
	private Chord(String name) throws InvalidChordException {
		Matcher m = CHORD_RE.matcher(name.toLowerCase());
		if (m.matches()) {
			root = Note.lookup(m.group(1));
			String triad = m.group(2);
			String extras = m.group(3);
			String base = m.group(5);
			
			// components are arranged by their order value 			
			this.comps = new TreeSet<ChordComponent>();
			this.comps.add( ChordComponent.getByString(triad) );
			
			if (extras != null && !"".equals(extras)) {
				this.comps.add( ChordComponent.getByString(extras) );
			}
			
			// add the additional base note
			if (base != null) {
				// transpose it down an octave
				this.base = Note.lookup(base).transpose(-12);
			}
		} else {
			throw new InvalidChordException();
		}
	}
	
	public static Chord lookup(String name) {
		try {
			return new Chord(name);
		} catch (InvalidChordException e) {
			return null;
		}
	}
	
	public String getName() {
		StringBuffer sb = new StringBuffer();
		sb.append(root.getName());
		// append the components
		for (ChordComponent cp : comps) {
			sb.append(cp.getLabel());
		}
		if (base != null) {
			sb.append("/");
			sb.append(base.getName());
		}
		return sb.toString();
	}

	public Set<Note> getNotes() {
		SortedSet<Note> result = new TreeSet<Note>();
		result.add(root);
		// add components
		for (ChordComponent cp : comps) {
			for (int o : cp.getOffsets()) {
				result.add( root.transpose(o) );
			}
		}
		if (base != null) {
			result.add(base);
		}
		return result;
	}

	public Chord transpose(int steps) {
		Chord r = null;
		try {
			r = new Chord(getName());
			// now we replace the root
			r.root = root.transpose(steps);
			if (base != null) {
				r.base = base.transpose(steps);
			}
		} catch (InvalidChordException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o.getClass().equals(getClass())) {
			Chord other = (Chord) o;
			return other.getNotes().containsAll(getNotes()) && getNotes().containsAll(other.getNotes());
		}
		return super.equals(o);
	}
	
	@Override
	public String toString() {
		return getName()+" chord";
	}

}
