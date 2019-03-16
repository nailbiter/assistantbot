#
#===============================================================================
#
#         FILE: macro.pm
#
#  DESCRIPTION: :
#
#        FILES: ---
#         BUGS: ---
#        NOTES: ---
#       AUTHOR: YOUR NAME (), 
# ORGANIZATION: 
#      VERSION: 1.0
#      CREATED: 10/05/18 15:56:40
#     REVISION: ---
#===============================================================================
use strict;
use warnings;
use JSON;

#global const's
my %FIELDTYPES = (
    STRING => [qw(cronline name info onFailed category)],
    NUMBER => [qw(delaymin)],
    BOOLEAN => [qw(enabled)],
	OBJECT=> [qw(callback)]
);
 
#global var's
my @habits;

#procedures
sub makehabits{
    (my $startTime,my $endTime,my $count,my $name,my $delay,my %rest) = @_;
    my @times;
    for(($startTime,$endTime)){
        /([0-9]*):([0-9]*)/;
        my %time = (
            HOUR=>$1+0,
            MIN=>$2+0,
        );
        push(@times,\%time);
    }
    my $intervalMin = ($times[1]->{HOUR}-$times[0]->{HOUR})*60 + ($times[1]->{MIN}-$times[0]->{MIN});
    for(my $i = 0; $i < $count; $i++){
        my $time = int(($times[0]->{HOUR}*60 + $times[0]->{MIN}) + ($intervalMin*($i+0.5)/$count));
        makeHabit(sprintf("%d %d * * *",$time % 60,int($time/60)),sprintf("%s:%d",$name,$i+1),$delay,%rest);
    }
}
sub habitToString{
    my $habitRef = $_;
    my @values;
    for(@{$FIELDTYPES{STRING}}){
        if(exists $habitRef->{$_}){
            push(@values,sprintf("\"%s\":\"%s\"",$_,$habitRef->{$_}));
        }
    }
    for(@{$FIELDTYPES{OBJECT}}){
		if( defined $habitRef->{$_}) {
		  push(@values,sprintf("\"%s\":%s",$_,encode_json($habitRef->{$_})));
		}
	}
    for(@{$FIELDTYPES{NUMBER}}){
        push(@values,sprintf("\"%s\":%d",$_,$habitRef->{$_}));
    }
    for(@{$FIELDTYPES{BOOLEAN}}){
        push(@values,sprintf("\"%s\":%s",$_,$habitRef->{$_}?"true":"false"));
    }

	#FIXME: this is bad, cause it's irregular
	if(exists $habitRef->{items}){
		my @items = map {sprintf("\"%s\"",$_)} @{$habitRef->{items}};
		push(@values,sprintf("\"%s\":[%s]","checklist",join(",",@items)));
	}
    return "\t{".join(", ",@values)."}";
}
sub makeHabit{
    (my $cronline,my $habitName,my $delaymin,my %rest) = @_;
    my %habit = %rest;

    $habit{cronline} = $cronline;
    $habit{name} = $habitName;
    $habit{delaymin} = $delaymin;

    $habit{info} //= "";
    $habit{onFailed} //= "move:todo";
    $habit{enabled} //= 1;

    push(@habits,\%habit);
}

END{
    printf("[\n");
    printf("%s\n",join(",\n",map(habitToString,@habits)));
    printf("]");
}

#main
