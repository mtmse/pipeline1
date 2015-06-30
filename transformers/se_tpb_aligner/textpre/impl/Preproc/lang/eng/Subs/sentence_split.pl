#!/usr/bin/perl -w

sub sentence_split {
	
	my $string = shift;

	# Break at major delimiters
	$string		=~	s/([$maj_del]+) /$1<SENT_SPLIT>/g;

	# ."
	$string		=~	s/([$maj_del])<SENT_SPLIT>([$quote])/$1$2<SENT_SPLIT>/g;

#	print "NU $string\n\n";

	# Name initials:	M. Karlsson
	$string		=~	s/\b([A-Z���]\.)<SENT_SPLIT>( *[A-Z���][a-z���]+)/$1$2/g;
	$string		=~	s/(<SENT_SPLIT>)+/<SENT_SPLIT>/g;

#	print "NU2 $string\n\n";

	# Name initials:	H.C.
	$string		=~	s/\.<SENT_SPLIT>([A-Z���](?:\.| ))/\.$1/g;
	$string		=~	s/\.<SENT_SPLIT>([A-Z���](?:\.| ))/\.$1/g;

	# c. AD90
	$string		=~	s/\.<SENT_SPLIT>( *[A-Z���][A-Z���])/\.$1/g;

	# Dates
	$string		=~ 	s/($abbreviation_list)<SENT_SPLIT>(\d)/$1 $2/ig;
	
	return $string;	
}
1;
