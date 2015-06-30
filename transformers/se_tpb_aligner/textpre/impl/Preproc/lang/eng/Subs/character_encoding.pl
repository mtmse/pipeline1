#!/usr/bin/perl -w

sub character_encoding {

	my $string = shift;

	$string =~ s/ç/�/g;	# c krok
	$string =~ s/Â/�/g;	# a tak
	$string =~ s/Ù/�/g;	# U bak�t
	$string =~ s/á/�/g;	# a med accent
	$string =~ s/Á/�/g;	# A accent
	$string =~ s/ä/�/g;	# a prick
	$string =~ s/Ä/�/g;	# A prick
	$string =~ s/å/�/g;	# a ring
	$string =~ s/Å/�/g;	# A ring
	$string =~ s/é/�/g;	# e accent
	$string =~ s/É/�/g;	# E accent
	$string =~ s/ë/�/g;	# e prick
	$string =~ s/Ë/�/g;	# E prick
	$string =~ s/í/�/g;	# i accent
	$string =~ s/Í/�/g;	# I accent
	$string =~ s/ï/�/g;	# i prick
	$string =~ s/Ï/�/g;	# I prick
	$string =~ s/ó/�/g;	# o accent
	$string =~ s/Ó/�/g;	# O accent
	$string =~ s/ö/�/g;	# o prick
	$string =~ s/Ö/�/g;	# O prick
	$string =~ s/ú/�/g;	# u accent
	$string =~ s/Ú/�/g;	# U accent
	$string =~ s/ü/�/g;	# u prick
	$string =~ s/Ü/�/g;	# U prick
	$string =~ s/ý/�/g;	# y accent
	$string =~ s/Ý/�/g;	# Y accent
	$string =~ s/ÿ/�/g;	# y prick
	$string =~ s/ñ/�/g;	# n tilde
	$string =~ s/Ñ/�/g;	# N tilde
	$string =~ s/è/�/g;	# e bak�taccent
	$string =~ s/È/�/g;	# E bak�taccent
	$string =~ s/ò/�/g;	# o bak�taccent
	$string =~ s/Ò/�/g;	# O bak�taccent
	$string =~ s/�/�/g;	# a bak�taccent
	$string =~ s/À/�/g;	# A bak�taccent
	$string =~ s/’/\'/g;	# enkelfnutt
	
	
	return $string;
}
1;
