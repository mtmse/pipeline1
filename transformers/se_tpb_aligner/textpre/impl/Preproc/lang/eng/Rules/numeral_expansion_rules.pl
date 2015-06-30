#!/usr/bin/perl -w

#**************************************************************#
# num_card_expansion
#
# Cardinals
# Numbers tagged as cardinals and unknown numbers.
#
#**************************************************************#
sub num_card_expansion {


	if (
		$pos{ $curr }	=~	/^(?:NUM CARD|NUM)$/


	) {

		my $ort		=	$ort{ $curr };
		$ort		=~	s/[ \,]//g;
		
		my $s = 0;
		if ( $ort =~ s/s$// ) {
			$s = 1;
		}

#		print "POS $pos{$curr}\nORT $ort\n";

		#**********************************************#
		# Conversion into letter string.
		#**********************************************#
		# Roman numbers
		if ( $type{ $curr }	=~	/ROMAN/ ) {
			my $arabic	=	&roman2arabic( $ort );
			$letters	=	&expand_cardinals( $arabic );
			
		# Arabic numbers
		} else {
			$letters	=	&expand_cardinals( $ort );
		}

		$letters	=	&insert_and($letters);

		
		#**********************************************#
		# Conversion into phoneme string.
		#**********************************************#
		my $transcription	=	&num_phonemes( $letters );

#		print "OOO $letters\t$transcription\n";

		if ( $s == 1 ) {
			$letters .= 's';
			$transcription .= ' s';
		}

		$exp{ $curr }		=	$letters;
		$transcription{ $curr }	=	$transcription;
		
#		print "L: $letters\tT: $transcription\n\n";
	}
}
#**************************************************************#
# num_year_expansion
#
# Years
#		
#**************************************************************#
sub num_year_expansion {

	if (
		$pos{ $curr }	=~	/YEAR/
	) {

		my $temp_year = $ort{ $curr };
	
		my $s = 0;
		if ( $temp_year =~ s/s$// ) {
			$s = 1;
		}


		#**********************************************#
		# Conversion into letter string.
		#**********************************************#
		my $letters	=	&expand_years( $temp_year );
		$letters	=	&insert_and($letters);

#		print "HHHH $ort{ $curr}\t$letters\n";
		
		#**********************************************#
		# Conversion into phoneme string.
		#**********************************************#
		my $transcription	=	&num_phonemes( $letters );

		if ( $s == 1 ) {
			$letters .= 's';
			$transcription .= ' s';
		}

		$exp{ $curr }		=	$letters;
		$transcription{ $curr }	=	$transcription;

#		print "L: $letters\tT: $transcription\n\n";
		
	}

}
#**************************************************************#
# num_ord_expansion
#
# Ordinals
#		
#**************************************************************#
sub num_ord_expansion {

	if (
		$pos{ $curr }	eq	"NUM ORD"
		&&
		$ort{ $curr }	=~	/\d/
	) {

#		print "\n-------------\nORD: $ort{ $curr }\n\n";

		my $ort		=	$ort{ $curr };
		$ort		=~	s/(?:$num_ord_ending)$//;

		#**********************************************#
		# Conversion into letter string.
		#**********************************************#
		# Roman numbers
		if ( $type{ $curr }	=~	/ROMAN/ ) {
			my $arabic	=	&roman2arabic( $ort{ $curr });
			$letters	=	&expand_cardinals( $arabic );

			
		# Arabic numbers
		} else {
			$letters	=	&expand_cardinals( $ort{ $curr } );
		}

		$letters	=	&insert_and($letters);

		# 3:e, 8:onde a.s.o.
		$letters	=~	s/one$/first/;
		$letters	=~	s/two$/second/;
		$letters	=~	s/three$/third/;
		$letters	=~	s/four$/fourth/;
		$letters	=~	s/five$/fifth/;
		$letters	=~	s/six$/sixth/;
		$letters	=~	s/seven$/seventh/;
		$letters	=~	s/eight$/eighth/;
		$letters	=~	s/nine$/nineth/;
		$letters	=~	s/ten$/tenth/;
		$letters	=~	s/eleven$/eleventh/;
		$letters	=~	s/tolv$/twelfth/;
		$letters	=~	s/(thirteen|fourteen|fifteen|sixteen|seventeen|eighteen|ninetten)$/$1th/;
		$letters	=~	s/(twent|thirt|fort|fift|sixt|sevent|eight|ninet)y$/$1ieth/;
		$letters	=~	s/hundred$/hundredth/;
		$letters	=~	s/thousand$/thousandth/;
		$letters	=~	s/million$/millionth/;

		#**********************************************#
		# Conversion into phoneme string.
		#**********************************************#
		my $transcription	=	&num_phonemes( $letters );


#		print "LL. $letters\tTT. $transcription\n\n";
		$exp{ $curr }		=	$letters;
		$transcription{ $curr }	=	$transcription;
	}



}
#**************************************************************#
sub expand_cardinals {

	my ($num) = @_;

	my @num_exp = ();	
	my $num_exp;


	# 00
	if ($num =~ /^00$/) {
		$num_exp = "oh oh";

	# 000
	} elsif ($num =~ /^000$/) {
		$num_exp = "oh oh oh";

	# Initial "0", spell the number.
	# The number is part of email/url/file name and 
	# contains more than five letters.
	} elsif (
		$num =~ /^0/
		||
		(
			$type{ $curr }	=~	/(?:EMAIL|URL|FILE NAME)/
			&&
			$num		=~	/\d\d\d\d\d/
		)
	
	) {
		
		my @num_list = split//,$num;
		
		foreach my $nl (@num_list) {

			# 0-9
			push @num_exp,&num_0_9($nl);

		}

		$num_exp = join" ",@num_exp;
	
	# Normal numbers (no initial zeroes).
	} else {

		
		# Split into 3-digit clusters.
		until ($num !~ /\d\d\d\d(\s|$)/) {
			$num =~ s/(\d\d\d)(\s|$)/ $1$2/;
		}

		my @num_list = split/ /,$num;
	
		foreach my $nl (@num_list) {

			# 0-9
			if ($nl =~ /^(\d)$/) {
				push @num_exp,&num_0_9($nl);
				$x = &num_0_9($nl);
			
			# 10-19
			} elsif ($nl =~ /^1\d$/) {
				push @num_exp,&num_10_19($nl);
				
			# 20-99
			} elsif ($nl =~ /^\d\d$/) {
				push @num_exp,&num_20_99($nl);
			
			# 100-999
			} elsif ($nl =~ /^\d\d\d$/) {
				push @num_exp,&num_100_999($nl);
	
			}
			
		} # end foreach $nl
	
	
		$num_exp = join" ",@num_exp;

	
		if ($num_exp !~ /^oh(?: oh)*$/) {
		
			# Skriv ihop om det �r ett j�mnt thousandtal, annars is�r.
			#if ($num =~ /000$/) {
			#	$num_exp =~ s/([a-z���]) ([a-z���]+)$/$1thousand $2/;
			#} else {
			#	$num_exp =~ s/([a-z���]) ([a-z���]+)$/$1 thousand $2/;
			#}
	
			# Insert "thousand".
			$num_exp =~ s/([a-z]) ([a-z]+)$/$1 thousand $2/;
		
		
			# Insert "millions" and "billions".
			$num_exp =~ s/([a-z]) ([a-z]+( thousand) [a-z]+)$/$1 millions $2/;
			$num_exp =~ s/([a-z]) ([a-z]+( millions?) [a-z]+( thousand) [a-z]+)$/$1 billions $2/;
		
			$num_exp =~ s/oh/ /g;
			$num_exp =~ s/tw u/twohu/g;
			
			$num_exp =~ s/(^| )hundred(thousand)?/$1 /g;
			$num_exp =~ s/(millions|billions)\s*(millions|thousand)/$1/g;
			$num_exp =~ s/(millions|billions)\s*(millions|thousand)/$1/g;
		
			$num_exp =~ s/ +/ /g;
		
			$num_exp =~ s/(^| )one (million|billion)s?/$1one $2/;
			$num_exp =~ s/(billions?) millions?/$1/;
			$num_exp =~ s/ +$//;
			
		} # end if $num_exp !~ oh oh

	} # end if �rtal med fyra siffror

	
	return $num_exp;
}
#**************************************************************#
# 0-9
#**************************************************************#
sub num_0_9 {
	my $num = shift;
	my $num_exp = $num_0_9{$num};
	
	return $num_exp;
}
#**************************************************************#
# 10-19
#**************************************************************#
sub num_10_19 {
	my $num = shift;
	my $num_exp = $num_10_19{$num};
	
	return $num_exp;
}
#**************************************************************#
# Pusslar ihop tal mellan 20 och 99.
#**************************************************************#
sub num_20_99 {
	my $num = shift;
	my ($first,$second) = split//,$num;
	my $first_exp = "oh";
	if ($first != 0) {
		$first_exp = $num_20_90{$first};
	}
	
	my $second_exp = $num_0_9{$second};


	my $num_exp = $first_exp . $second_exp;

	return $num_exp;
}
#**************************************************************#
# Pusslar ihop tal mellan 100 och 999.
#**************************************************************#
sub num_100_999 {
	my $num = shift;
	my $second_exp;
	my ($first,$second,$third) = split//,$num;

	# F�rsta siffran + "hundred".
	my $first_exp = $num_0_9{$first} . "hundred";	

	$second .= $third;
	
	# Om andra siffran �r "1", d.v.s. ett 10-tal.
	if ($second =~ /^1/) {
		$second_exp = $num_10_19{$second};
		
	# Annars �r det 20-99.
	} else {
		$second_exp = &num_20_99($second);
	}

	my $num_exp = $first_exp . $second_exp;

	return $num_exp;	
}
#**************************************************************#
# num_phonemes
#
#
#**************************************************************#
sub num_phonemes {

	my $ort = shift;
	my @ort = split/ +/,$ort;
	my $trk;

	
	foreach my $temp_ort (@ort) {

		# Split to separate words (except 1-19).
		$temp_ort =~ s/((?:thousand|hundred|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninety)(?:th[^r])?)/ $1 /g;
		
		
		$temp_ort =~ s/^ +//; $temp_ort =~ s/ +$//;

		my @temp_ort = split/ +/,$temp_ort;
		my $temp_trk;

		# Lookup each numeral chunk.
		foreach my $temp_o (@temp_ort) {
			$temp_o =~ s/^ +//; $temp_o =~ s/ +$//;

			# print "TO $temp_o\n";

			if (exists($num_trk{$temp_o})) {
				$o_trk = $num_trk{$temp_o};
			
			} elsif ($temp_o eq "and") {
				$o_trk = "'�3: n d";
			}
			
			$temp_trk .= " $o_trk";
	
			
		} # end foreach $temp_o
		
		# Stress
		#$temp_trk	=	&num_stress($temp_trk);
		
		$trk		.=	" $temp_trk";
		
	} # end foreach $ort
	
	$trk =~ s/^ +//;
	$trk =~ s/ +$//;
	$trk =~ s/ +/ /g;

	return $trk;	
}
#**************************************************************#
# num_stress
#
# Removing stress symbols from concatenated numeral transcription.
#
#**************************************************************#
sub num_stress {
	
	my ($trk) = @_;

	# Remove stress at final "hundred".
	if ($trk =~ s/([a-z2\:\.]) h \'a n d r i d *$/$1 h a n d r i d/) {

	# Remove multiple main stress.
	} elsif ($trk =~ /^(.*[\"\'].*)([\"\'][^\"\']+)$/) {
		my $first = $1;
		my $second = $2;
			
		# Save only the last stress symbol.
		$first =~ s/[\"\'\`]//g;

		$trk = $first . $second;	
	}
	return $trk;	
}
#**************************************************************#
# expand_years
#
# Expanding years.
#**************************************************************#
sub expand_years {
	
	my $num = shift;	
	my $num_exp;
	
	# 4 digits
	if ($num =~ /^([1-9]\d)(\d\d)$/) {
		
		my $first = $1;
		my $second = $2;
		
		if ($first =~ /^1/) {
			
			$num_exp	=	&num_10_19($first) . " ";
			
		} elsif ($first =~ /20/) {
			
			$num_exp	=	"two thousand ";
			
		} elsif ($first =~ /21/) {
			
			$num_exp	=	"twentyone ";
			
		}
			
		if ( $second eq "00" && $first ne '20' ) {
			
			$num_exp	.=	"hundred";
			
		} elsif ($second =~ s/0([1-9])/$1/) {
			
			$num_exp	.=	&num_0_9($second);
			
		} elsif ($second =~ /^1/) {
			
			$num_exp	.=	&num_10_19($second);
			
		} elsif ($second =~ /^[2-9]/) {
			
			$num_exp	.=	&num_20_99($second);
			
		}
		

		$num_exp	=~	s/oh$/ /;
		
	
	# Not 4 digits, process as normal cardinal.
	} else {
		$num_exp = &expand_cardinals( $num );
	}
	
	return $num_exp;
	
}
#**************************************************************#
# Insert "and" before last digit chunk after 
# "million", "thousand" and "hundred"
#**************************************************************#
sub insert_and {

	my $letters = shift;
	
	# Split at "billion", "million", "thousand" and "hundred"
	$letters =~ s/(billions?|millions?|thousands?|hundreds?)(one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve|thir|fif|twen)/$1 $2/g;

	my @letters = split/(billions?|millions?|thousands?|hundreds?)/,$letters;
	
	my $counter = 0;
	foreach $l (@letters) {
#		print "LL $l\n"; 
		if (
			$l		=~	/hundred/
			&&
			$counter	!=	$#letters
		) {
			$l		.=	" and ";
			
		} elsif (
			(
				$l			=~	/(?:billions?|millions?|thousand)/
				&&
				$counter		<=	$#letters-2
				&&
				$letters[$counter+2]	!~	/(?:billions?|millions?|thousand|hundred)/
			) || (
				$l			=~	/(?:billions?|millions?|thousand)/
				&&
				$counter		>	$#letters-2
			)
		) {
			$l		.=	" and ";
		}

		$counter++;
	}
	
	$letters = join"",@letters;
	
	$letters =~ s/ *and *$//;
		
	$letters = &clean_blanks($letters);

	return $letters;
}
#**************************************************************#
# roman2arabic	Converting roman numbersr to arabib numbers.
#		
#**************************************************************#
sub roman2arabic {


    my($arg) = shift;

	

    %roman2arabic = qw(I 1 V 5 X 10 L 50 C 100 D 500 M 1000);
	
    my($last_digit) = 1000;
    my($arabic);
    foreach (split(//, uc $arg)) {
        my($digit) = $roman2arabic{$_};
        $arabic -= 2 * $last_digit if $last_digit < $digit;
        $arabic += ($last_digit = $digit);
    }
    
    return $arabic;
}
#**************************************************************#
1;
