#!/usr/bin/perl
# LEGAL WARNING:
#
# This software is provided on an "AS IS", basis,
# without warranty of any kind, including without
# limitation the warranties of merchantability, fitness for
# a particular purpose and non-infringement. The entire
# risk as to the quality and performance of the Software is
# borne by you. Should the Software prove defective, you
# and not the author assume the entire cost of any service
# and repair.
#

# online documentation can be converted to html
# using esd2html.pl from http://peter.verhas.com

=pod
=H jamal main file
=abstract
This is the main Perl file for the jamal distribution.
=end
The functions and other entities are defined here in the order
as they appear in the source file. In case you are interested in
writing extensions for jamal in Perl see the functions that are in
the package jamal. These can be found after the chapter T<packageJamal>.

See the chapter R<packageJamal> for rules that a jamal compliant extension
should implement.
=cut

=pod
=section history
=H Jamal version history

=itemize
=item 3.02 bug fix to be able to handle large 'define's
=item 3.01 bug fix to prevent Jamal into infinite loop when macro parameters have a trailing separator May 15, 2006.
=item 2.03 {#define? ...} and {?macro} feature introduced
=item 2.021 {#define A/X=BXB}{A 0} formerly resulted BB. Now it works fine (results B0B, where 0 is a zero not an oh).
=item 2.02 {#sep[[/]]} drowe jamal to infinite loop (macro open char is null string)
=item ---- create directory for trace and output if it did not exist
=item 2.01 {#-t operator is introduced for file time
=item ----  line number tracking for error messages
=item ---- first operand bug of 'and' and 'or' fixed
=item ---- new operator 'not' (how could i miss it?)
=item 2.0 release includes Perl extension interface
=item 1.0 release
=item 1.0beta6 split macro to that many argument as many needed. The last contains the 'rest'
=item 1.0beta5 correction to handle new line as a list separator correctly
=item 1.0beta4 first public release December 15, 1997.
=noitemize
=cut

*VERSION = \'3.02';                        # the version of jamal

*umask = \0777;                            # directory permission when a new dir is created

$timeNow = time();                         #

*SYSmacroOpen  = \'{';                     # macro opening string default value
*SYSmacroClose = \'}';                     # macro closing string default value

$macroOpen  = $SYSmacroOpen;               # current macro opening string
$macroClose = $SYSmacroClose;              # current macro closing string

$MacroOpen  = quotemeta $macroOpen;        # current macro opening match
$MacroClose = quotemeta $macroClose;       # current macro closing match

$mopenCNL = &CNL($macroOpen);              # new line characters in macro opening string
$mcloseCNL = &CNL($macroClose);            # new line characters in macro closing string
                                           # CNL=Count New Lines

@MacStack = ();

# preset command line arguments
$OutputError = 0; #by default erroneous macros result empty string
$SuppressOutput = 0;
$TraceFile = undef;
$LastFileNameDisplayedOnTraceOutput = '';
$Source = undef;
$Destination = undef;
@Macrofiles = ();

# Hashes to store macros and argument lists
%Macro = ();  # $Macro {'name'} = 'body of the macro';
%MacArg = (); # $MacArg{'name'} = ref to argument string array

# Stack to store old macro values when they are redefined w/o undefing first
%MacroStack = ();

# The loaded extension packages
%Module = ();
# minimal and maximal version numbers of a module
%minV = (); %maxV = ();
# required jamal version of a module
%ModRequire = ();

# to store the current file list along includes (how did we get to the file
# we are currently prpcessing, who included who, and at what line in which file)
@FILE = ();
%LINE = ();

# base module stack
@BaseModules = ();
$BaseModule = undef; # no base module by default
# a global variable telling jamal that an
# extension macro should not be processed
# after it has been evaluated
$ExtensionVerbatim = 0;


while( $_ = shift @ARGV ){

  if( s/^\-// ){#this is option
    if( $_ eq '?' || $_ eq 'h' ){
      &help; exit;
      }
    if( $_ eq 'm' ){
      push @Macrofiles, shift @ARGV;
      next;
      }
    if( $_ eq 'I' ){
      push @INC, shift @ARGV;
      next;
      }
    if( $_ eq '0' ){
      $SuppressOutput = 1;
      next;
      }
    if( $_ =~ /\D(\w+)((?:.|\n)*?)=((?:.|\n)*)$/ ){
      &DefineMacro($1,$2,$3);
      next;
      }
    if( $_ eq 'd' ){
      $OutputError = 1;
      next;
      }
    if( $_ eq 't' ){
      $TraceFile = shift @ARGV;
      next
      }
    &Error("Invalid option. Ignored.",$_);
    }else{
    # file name source then destination
    unless( defined($Source) ){
      $Source = $_; next;
      }
    unless( defined($Destination) ){
      $Destination = $_; next;
      }
    # if both files are defined
    print STDERR "There are too many command line arguments\njamal -?";
    exit;
    }

  }

unless( defined $Source ){
  &Error('No input is defined.');
  exit;
  }

if( $TraceFile && &maked($TraceFile) && !open(TRACE,">$TraceFile")){
  &Error("Trace file can not be opened.",$TraceFile);
  $TraceFile = undef;
  }

# make file names UNIX compliant under Windows NT
# this should not harm under UNIX (usually :-)
# don't be pervert to include a \ in a file name
$Source =~ s{\\}{/}g;
$Destination =~ s{\\}{/}g;


&PredefineMacro;

for $macrofile ( @Macrofiles ){
  &DoFile($macrofile);
  }

if( $Destination && &maked($Destination) && !open($Destination,">$Destination") ){
  &Error('Output file can not be opened.',$Destination);
  exit;
  }

$output = &DoFile($Source);
if( $Destination ){
  print $Destination $$output;
  close $Destination;
  }else{ print $$output unless $SuppressOutput }

#we are ready
exit;

=pod
=section DoFile
=H Process an input file

This function gets an imput file name, reads the content and
processes all jamal macros in it. The result is put into a
string buffer and reference to it is returned.

Usage:
=verbatim
           $sref = &DoFile('filename');
           print $$sref; # dereference
=noverbatim

For the actual processing function T<DoInput> is invoked.

=cut
sub DoFile {
  my $file = shift;
  my $output = '';

  push @FILE,$file;
  $LINE{$file} = 1;

  unless( open($file,"<$file") ){
    print STDERR "Input file '$file' cannot be opened.\n";
    if( $#FILE > 0 ){
      print STDERR " Include hierarchy leading to this file name:\n";
      pop @FILE;
      my $lastFile = pop @FILE;
      my $spc = 0;
      for( @FILE ){
        print STDERR ' ' x $spc++," file $_ at line ",$LINE{$_}," included\n";
        }
      print STDERR ' ' x $spc++," file $lastFile at line ",$LINE{$lastFile}," tried to include this file\n";
      }
    return \'';
    }
  my $o_irs = $/; undef $/;
  my $input = <$file>;
  close $file;
  $/ = $o_irs;

  # If the last character on a line is \ and the very first character on the next line is
  # _ (underscore) then the backslash, the newline, the underscore and the whitespaces on that
  # line are removed. This helps tabulating in an environment where spaces play important role
  $input =~ s{\\\n\_\s*}{}g;

  # The \ character at the end of a line removes the new line character
  # this can be extremely useful in fine tuned HTML code
  $input =~ s{\\\n}{}g; # escape new lines
  $input =~ s{\\$}{};   # last line end w/o new line

  # note that here we loose the line numbering precision
  # therefore line numbers in the error messages only correlate
  # with the real line numbers, and equals the real line numbers
  # only if the source file does not use new line escape

  my $return = &DoInput( \$input , $file , 0);
  pop @FILE;
  return $return;
  }

=pod
=section DoInput
=H Process an input string
Get a string, resolve macro references and return a pointer
to the result.

Usage:
=verbatim
           $sref = &DoInput( \$input , $file , $level );
           print $$sref; # dereference
=noverbatim
The second parameter T<$file> is needed only to resolve T<{#include}>
macros with relative directory reference.

The third parameter T<$level> gives the actual deepness of the code. This is zero
for characters coming from the input file. It is T<n> for characters generated by
macros of the level T<n-1>. It is used to track line numbers for error messages.

The actual algorithm of T<DoInput> is very simple. It takes all character that do not
belong to a macro and appends to the output which has zero length at start. When it
finds the start of a macro it chops it off calling T<ChopMacro>, and evaluates the macro
calling T<EvalMacro> and appends the result to the output and goes on for other macros.

It actually calls itself recursively as well as the called T<EvalMacro> may also
call T<DoInput> recursively.
=cut
sub DoInput {
  my $input = shift;
  my $file  = shift;
  my $level = shift;
  my $output = '';
  while( length($$input) > 0 ){
    if( index($$input,$macroOpen) == 0 ){
      $$input = substr($$input,length($macroOpen));
      $LINE{$file} += $mopenCNL unless $level;

      #chop off the macro and return the string
      my $macro = &ChopMacro($input,$level);

      &Trace('M',$macro,$level);

      # calculate the line we are going to be after this macro is processed
      my $LineNR = $LINE{$file} + &CNL($$macro) + $mcloseCNL;

      #if the macro starts with a @ character then macros used
      #within are not substituted before processing the macro only after
      unless( substr($$macro,0,1) eq '@' ){
        my $oldLine = $LINE{$file};
        $macro    = &DoInput($macro,$file,$level);
        $LINE{$file} = $oldLine;
        &Trace('M',$macro,$level);
        }

      # evaluate the macro and set the new line number if it was top level macro
      $output  .= &EvalMacro($macro,$file,$level);
      $LINE{$file} = $LineNR unless $level;
      }else{
      # Former version used pattern matching but Perl was not
      # capable handling some of the large strings. This should also
      # be faster:
      my $i;
      if( -1 < ($i = index($$input,$macroOpen)) ){
        $lll = substr($$input,0,$i);
        $output .= $lll;
        &Trace('L',\$lll,$level);
        $LINE{$file} += &CNL($lll) unless $level;
        $$input = substr($$input,$i);
        }else{
        &Trace('L',$input,$level);
        $output .= $$input;
        $LINE{$file} += &CNL($$input) unless $level;
        $$input = '';
        }
      }
    }
  return \$output;
  }

=pod
=section ChopMacro
=H Chop macro off a string

Chop off a macro from the start of the input string, (passed by reference)
put it into a new buffer and return the reference of the
buffer. The input string passed by reference does NOT contain the opening
macro brace, it should already be chopped off.

Usage:
=verbatim
          $sref = &ChopMacro( \$input);
=noverbatim

The result string will not contain the opening and closing
brace (or whatever the actual value of T<MactoOpen> and T<MacroClose>

are.

The function takes care of the embedded macros counting up for each opening brace
and counting down for each closing brace. When counting down result zero, the end of the
macro is found.

If the counter does not reach zero till the end of the file there is a macro nesting
error, in other words more opening than closing brace. In such a case the function
T<Error> is called and the function calls T<exit>.

=cut
sub ChopMacro {
  my $input = shift;
  my $Counter = 1;# we are after one macro opening brace
  my $output = '';
  my $chop;

  while( $Counter ){# while there is any opened macro
    if( length($$input) == 0 ){# some macro was not closed
      &Error('Erroneous macro nesting.',$output);
      return \$output;
      }
    if( index($$input,$macroOpen) == 0 ){
      $$input = substr($$input,length($macroOpen));
      $Counter ++; #count the new opening
      $output .= $macroOpen;
      }
    elsif( index($$input,$macroClose) == 0 ){
      $$input = substr($$input,length($macroClose));
      $Counter --; # count the closing
      return \$output unless $Counter > 0;
      $output .= $macroClose;
      }else{
      # Former version used pattern matching but
      # Perl had difficulties to handle large
      # strings. This is a faster solution.

      # Go and find the next macro opening or closing brace
      my $i = index($$input,$macroOpen);
      my $j = index($$input,$macroClose);
      # whichever we have found and preceedes the other
      $i = $j if $j < $i && $j != -1 || $i == -1;
      if( -1 < $i){
        $output .= substr($$input,0,$i);
        $$input = substr($$input,$i);
        }else{
        $output .= $$input;
        $$input = '';
        }
      }
    }
  }

=pod
=section EvalMacro
=H Evaluate a macro

Evaluate the macro given as a string reference. Returns
the result as a string.

Usage:
=verbatim
            $output .= &EvalMacro(\$string,$filename,$level)
=noverbatim

T<$filename> is used merely to resolve relative file references in T<include>
macros.

The third parameter T<$level> gives the actual deepness of the code. This is zero
for characters coming from the input file. It is T<n> for characters generated by
macros of the level T<n-1>. It is used to track line numbers for error messages.

This function first checks if there is a T<@verbatim> modifier before the macro name.

After this: if the macro starts with T<#> or T<@> then it is a built in macro
otherwise this is user defined or predefined macro (functinally there are no differences).

Finally T<DoInput> is called in most cases to resolve macros in the resulted string.

When a macro starts with the T<$> character it is a loop macro and is processed only if
it is in a loop. Otherwise the result is the macro itself.
=cut
sub EvalMacro {
  my $input = shift;
  my $ReferenceFileName = shift;
  my $level = shift;
  my $verbatim = 0;
  my $ReportNotDefinedMacro = 1;
  $verbatim = 1 if $$input =~ s/^(?:#|@)verbatim\s+// ;

  my $char,$index=0; while( ($char=substr($$input,$index,1)) eq ' ' ||
                             $char eq "\n" || char eq "\t" || char eq "\r" ){ $index++ }
  if( ($char = substr($$input,$index,1)) eq '@' || $char eq '#' ){
    $index ++;
    while( ($char=substr($$input,$index,1)) eq ' ' ||
            $char eq "\n" || char eq "\t" || char eq "\r" ){ $index++ }
    $$input = substr($$input,$index);
    $index = 1; # this is built in macro
    }else{
    if( substr($$input,$index,1) eq '?' ){
      $index++;
      while( ($char=substr($$input,$index,1)) eq ' ' ||
              $char eq "\n" || char eq "\t" || char eq "\r" ){ $index++ }
      $ReportNotDefinedMacro = 0;
      }
    $$input = substr($$input,$index) if $index > 0;
    $index = 0; # this is user defined macro
    }

  if( $index ){
    # Perl module extension macro.
    if( $$input =~ /^\w*\:\:/ ){#/^(\w*)((?:\:\:\w+)+)/ ){
      $$input =~ s/^(\w*)//;
      my $base = $1;
      $$input =~ s/((?:\:\:\w+)+)//;
      my $macro = $1;
      $base = $BaseModule unless $base;
      $@ = '';
      local $ExtensionVerbatim = 0;
      local $InputFileName = $ReferenceFileName;
      local $OutputFileName = $Destination;
      my $result = eval "&jamal::$base$macro(" . '$$input)';
      if( ! $@ ){
        return $result if $ExtensionVerbatim ;
        $result = &DoInput(\$result,$ReferenceFileName,$level+1);
        return $$result;
        }else{
        return &Error("Extension macro resulted the error: $@",$$input);
        }
      }

    if( $$input =~ /^require(\W.*)$/ ){
      my $rversion = &Split($1,1);
      $rversion = $rversion->[0];
      if( $rversion > $VERSION ){
        &Error("This file requires jamal V$rversion and I am only $VERSION",$$input);
        exit;
        }
      return '';
      }

    if( $$input =~ /^with(\W.*)$/ ){
      my $package = &Split($1,1);
      if( $$package ){
        push @BaseModules,$BaseModule;
        $BaseModule = $$package;
        }else{ $BaseModule = pop @BaseModules }
      return '';
      }

    # {#use package version}
    if( $$input =~ /^use(\W.*)$/ ){
      my ($package,$version) = @{Split($1,2)};
      $version = '' unless defined $version;
      if( defined $Module{$package} ){
        &Warning("Extension $package is already loaded.",$$input);
        }
      $@ = '';
      $Module{$package} = $version;
      $jamal::VersionWasCalled = 0; $jamal::RequireWasCalled = 0;
      eval "use jamal::$package";
      if( ! $@ ){
        if( $version && (
            (defined($maxV{$package}) && $maxV{$package} < $version) ||
            (defined($minV{$package}) && $minV{$package} > $version) )){
          &Error("Extension $package does not implement the requested version $version.",$$input);
          exit;#this is a severe error
          }
        if( ! $jamal::VersionWasCalled ){
          &Error("Extension $package does not specify its own version.",$$input);
          }
        if( ! $jamal::RequireWasCalled ){
          &Error("Extension $package does not specify the jamal version it requires.",$$input);
          }
        push @BaseModules,$BaseModule;
        $BaseModule = $package;
        return '';
        }else{
        delete $Module{$package};
        return &Error("Extension $package is not found. Error message: $@",$$input);
        }
      }

    if( $$input =~ /^sep(\W.*)$/ ){
      my $seps = $1;
      my ($mO,$mC) = @{&Split($seps,2)};
      if( $mO eq '' ){
        &Error("Semantic error in macro SEP would result empty macro open string",$$input);
        return '';
        }
      if( $mC eq '' ){
        &Error("Semantic error in macro SEP would result empty macro close string",$$input);
        return '';
        }
      $macroOpen = $mO;
      $macroClose = $mC;
      $MacroOpen = quotemeta $macroOpen;
      $MacroClose = quotemeta $macroClose;
      $mopenCNL = &CNL($macroOpen);
      $mcloseCNL = &CNL($macroClose);
      return '';
      }

    if( $$input =~ /^for\s+(\w+)\s*((?:.|\n)*)$/ ){
      my $loopV = quotemeta $1;
      my $loop = $2;
      $loop =~ s/^(.|\n)//;
      my $term = &CharPair($1);
      $loop =~ s/^((?:.|\n)*?)$term//;
      my $loopP = $1;
      if( $loopP =~ /^\s*((?:\+|\-)?\d+)\s*\.\.\.?\s*((?:\+|\-)?\d+)\s*$/ ){
#{#for i/ 1 ... 1000 / a[i]=i;}
        my $s = $1; my $e=$2;
        my $output = '';
        while(1){
          my $wp = $loop;
          $wp =~ s/$loopV/$s/g;
          $output .= $wp;
          last if $s == $e;
          if( $s < $e ){ $s++ }else{ $s-- }
          }
        $output = &DoInput(\$output,$ReferenceFileName,$level+1);
        return $$output;
        }
#{#for i/,1,2,3,4,kakukk/ a[i]=i;}
      my $argv = &Split( $loopP );
      my $output = '';
      for( @$argv ){
        my $wp = $loop;
        $wp =~ s/$loopV/$_/g;
        $output .= $wp;
        }
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }

#{#dir/directory/pattern/text}
    if( $$input =~ /^dir(\W(?:.|\n)*)$/ ){
      my $macro = $1;
      my $sortorder = '';
      my $html = 0;
      $macro =~ s/^(.|\n)//;
      my $sep = $1;
      if( $sep =~ /^\s+$/ ){
        $sep = '\s+';
        $macro =~ s/^\s*//;
        }else{ $sep = quotemeta $sep }
      $macro =~ m{(.*?)$sep(.*?)$sep((?:.|\n)*)$};
      my $rdir = $1;
      my $pattern = $2;
      my $body = $3;
      my $RefD = '';
      my $recurse = 0;
      my $pat = $pattern;
      while( $pat =~ s{^\s*\-}{} ){
        while( $pat =~ s{^(\w|\.)}{} ){
          my $f = $1;
          if( $f eq 'h' ){ $html = 1; next; }# get html parameters from the files
          if( $f eq 'S' ){
            $pat =~ s{^(n|t|s|d)(a|d)?}{};
            $html = 1 if $1 eq 't';
            $sortorder = $1 . ($2 ? $2 : 'a') ;
            next;
            }
          if( $f eq 'i' || $f eq 'o' ){
            if( $RefD ne '' ){
              &Error('Ambiguous directory definition.',$$input);
              next;
              }else{ $RefD = $f; next; }
            }
          if( $f eq 'R' ){ $recurse = 1; next; }
          next if $f =~ /\.|z|s|d|f/;
          &Error("Invalid dir option $f",$$input);
          }
        }
      $RefD = 'i' unless $RefD;
      my $dir;
      $dir = &Relative($ReferenceFileName,$rdir) if $RefD eq 'i' || ($RefD eq 'o' && !$Destination);
      $dir = &Relative($Destination,$rdir) if $RefD eq 'o' && $Destination;
      my @filist;
      if( $recurse ){
        opendir(D,$dir);
        my @f = readdir(D);
        closedir(D);
        my @DirsLeft = ();
        my $qdir = '';
        while(1){
          $qdir = $qdir . '/' if $qdir;
          for( @f ){
            if( -d "$dir/$qdir$_" ){
              push @DirsLeft , "$qdir$_" unless $_ eq '.' || $_ eq '..';
              }
            push @filist , "$qdir$_";
            }
          last if $#DirsLeft == -1;
          $qdir = pop @DirsLeft;
          opendir(D,"$dir/$qdir");
          @f = readdir(D);
          closedir(D);
          }
        }else{
        opendir(D,$dir);
        @filist = readdir(D);
        closedir(D);
        }
      while( $pattern =~ s{^\s*\-}{} ){
        # z zero size, s nonzero size, f plain file, d directory, . not . or ..
        while( $pattern =~ s{^(\w|\.)}{} ){
          my $f = $1;
          if( $f eq 'S' ){
            $pattern =~ s{^(n|t|s|d)(a|d)?}{};
            next;
            }
          @filist = grep( -z "$dir/$_" , @filist) if $f eq 'z';
          @filist = grep( -s "$dir/$_" , @filist) if $f eq 's';
          @filist = grep( -d "$dir/$_" , @filist) if $f eq 'd';
          @filist = grep( -f "$dir/$_" , @filist) if $f eq 'f';
          # exclude the . and .. directories for subdirectories as well
          @filist = grep( !/^\.$/ && !/^\.\.$/ && !/\/\.$/ && !/\/\.\.$/ , @filist) if $f eq '.';
          #ignore i, o, a and R options, they have already been taken care of
          }
        }
      $pattern =~ s{^\s*}{};
      if( $pattern ){
        $pattern = quotemeta $pattern;
        $pattern =~ s{\\\*}{\.*}g;
        @filist = grep( m/^$pattern$/ , @filist);
        }

      my @FileArray = ();
      for $filename (@filist){
        my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,
            $atime,$mtime,$ctime,$blksize,$blocks) = stat("$dir/$filename");
        my @params = ();
        push @params, ($filename, "$rdir/$filename", $size, &ConvertDate($mtime), &ConvertTime($mtime), $mtime );
        if( $html ){
          my ($title) = &HtmlParameters("$dir/$filename");
          push @params, $title;
          }else{ push @params, '' }
        push @FileArray, \@params;
        }

      @FileArray = sort { $a->[0] cmp $b->[0] } @FileArray if $sortorder eq 'na';
      @FileArray = sort { $b->[0] cmp $a->[0] } @FileArray if $sortorder eq 'nd';

      @FileArray = sort { $a->[6] cmp $b->[6] } @FileArray if $sortorder eq 'ta';
      @FileArray = sort { $b->[6] cmp $a->[6] } @FileArray if $sortorder eq 'td';

      @FileArray = sort { $a->[2] <=> $b->[2] } @FileArray if $sortorder eq 'sa';
      @FileArray = sort { $b->[2] <=> $a->[2] } @FileArray if $sortorder eq 'sd';

      @FileArray = sort { $a->[5] <=> $b->[5] } @FileArray if $sortorder eq 'da';
      @FileArray = sort { $b->[5] <=> $a->[5] } @FileArray if $sortorder eq 'dd';

      my $output = '';
      push @LoopMacroStack,$Macro{'$file$name'};
      push @LoopMacroStack,$Macro{'$file$url'};
      push @LoopMacroStack,$Macro{'$file$size'};
      push @LoopMacroStack,$Macro{'$file$date'};
      push @LoopMacroStack,$Macro{'$file$time'};
      push @LoopMacroStack,$Macro{'$html$title'};
      push @LoopMacroStack,$WeAreInLoop;
      $WeAreInLoop = 1;
      for $f (@FileArray){
        $f->[0] =~ m{(.*)\.(.*)};
        $Macro{'$file$nam'}  = $1;
        $Macro{'$file$e'}     = $2;
        $Macro{'$file$name'}  = $f->[0];
        $Macro{'$file$url'}   = $f->[1];
        $Macro{'$file$size'}  = $f->[2];
        $Macro{'$file$date'}  = $f->[3];
        $Macro{'$file$time'}  = $f->[4];
        $Macro{'$html$title'} = $f->[6];
        my $bdy = $body;
        $bdy = &DoInput(\$bdy,$ReferenceFileName,$level+1);
        $output .= $$bdy;
        }
      $WeAreInLoop = pop @LoopMacroStack;
      $Macro{'$html$title'}= pop @LoopMacroStack;
      $Macro{'$file$time'} = pop @LoopMacroStack;
      $Macro{'$file$date'} = pop @LoopMacroStack;
      $Macro{'$file$size'} = pop @LoopMacroStack;
      $Macro{'$file$url'}  = pop @LoopMacroStack;
      $Macro{'$file$name'} = pop @LoopMacroStack;
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }

    if( $$input =~ /^months(\W(?:.|\n)*)$/ ){
      my $macro = $1;
      my $list = &Split($macro,12);
      $MonthNames = $list;
      if( $#$list < 11 ){
        return &Error('There are not enough month names defined:' , $$input);
        }elsif( $#$list > 11 ){
        return &Error('There are too many month names defined:' , $$input);
        }
      return '';
      }

    if( $$input =~ /^days(\W(?:.|\n)*)$/ ){
      my $macro = $1;
      my $list = &Split($macro,7);
      $DayNames = $list;
      if( $#$list < 6 ){
        return &Error('There are not enough day names defined:' , $$input);
        }elsif( $#$list > 6 ){
        return &Error('There are too many day names defined:' , $$input);
        }
      return '';
      }

    if( $$input =~ /^format\s+time=((?:.|\n)*)$/ ){
      $TimeFormat = $1;
      return '';
      }
    if( $$input =~ /^format\s+date=((?:.|\n)*)$/ ){
      $DateFormat = $1;
      return '';
      }
    if( $$input =~ /^format\s+am=((?:.|\n)*)$/ ){
      $AM = $1;
      return '';
      }
    if( $$input =~ /^format\s+pm=((?:.|\n)*)$/ ){
      $PM = $1;
      return '';
      }

    # both macros are implemented in a way that pervert users
    # including macro reference in the time and date format
    # get the correct result
    if( $$input =~ /^time\s*$/ ){
      my $output = &ConvertTime($timeNow);
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }
    if( $$input =~ /^date\s*$/ ){
      my $output = &ConvertDate($timeNow);
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }

# {#select/n/,1,2,3,4,5}
    if( $$input =~ /^select(\W(?:.|\n)*)$/ ){
      my $macro = $1;
      my ($n,$list) = @{&Split($macro,2)};
      my @list = @{&Split($list)};
      my $output = $list[$n];
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }

# {#if/test/then/else}
    if( $$input =~ /^if(\W(?:.|\n)*)$/ ){
      my $macro = $1;
      my ($test,$then,$else) = @{&Split($macro,3)};
      my $output = $test ? $then : $else;
      $output = &DoInput(\$output,$ReferenceFileName,$level+1);
      return $$output;
      }

#{#null text}
    if( $$input =~ /^null\s*((?:.|\n)*)$/ ){
      return $1;
      }

#{#comment text}
    if( $$input =~ /^comment\s*((?:.|\n)*)$/ ){
      return'';
      }

# {#op /exp1/exp2/.../expn}
    my $f1 = substr($$input,0,1);
    my $f2 = substr($$input,0,2);
    my $f3 = substr($$input,0,3);
    my $match = 0;
    my ($op,$macro);

    if( $f3 =~ m{and|not} ){
      $match = 1;
      $op = $f3;
      $macro = substr($$input,3);
      }
    elsif( $f2 =~ m{<=|>=|!=|eq|ne|gt|lt|ge|le|or} ||
           $f2 =~ m{-(z|s|d|f|e|t)}i ){
      $match = 1;
      $op = $f2;
      $macro = substr($$input,2);
      }
    elsif( $f1 =~ m{^(=|<|>|\*|/|\+|-|)$} ){
      $match = 1;
      $op = $f1;
      $macro = substr($$input,1);
      }

    if( $match ){
      my $list = &Split($macro);
      my $fop = shift @$list;
      my $result = $fop;

      # unary file operators
      if( $op =~ s/\-(z|s|d|f|e|t)/$1/i ){
        if( $op eq lc $op || ! defined $Destination ){
          $fop = &Relative($ReferenceFileName,$fop);
          }else{
          $fop = &Relative($Destination,$fop);
          }
        $op = lc $op;
        if( $op eq 'z' ){
          if( -z $fop ){ return '1' }else{ return '0' }
          }
        if( $op eq 's' ){
          if( -s $fop eq '' ){ return '0' }else{ return -s $fop }
          }
        if( $op eq 'd' ){
          if( -d $fop ){ return 1 }else{ return 0 }
          }
        if( $op eq 'f' ){
          if( -f $fop ){ return 1 }else{ return 0 }
          }
        if( $op eq 'e' ){
          if( -e $fop ){ return 1 }else{ return 0 }
          }
        if( $op eq 't' ){
          my ($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,
              $atime,$mtime,$ctime,$blksize,$blocks) = stat($fop);
          return $mtime;
          }
        #you never get here
        }

      # unary minus
      if( $#$list == -1 && $op eq '-' ){
        $result = -1 * $fop;
        return $result;
        }

      # unary plus
      if( $#$list == -1 && $op eq '+' ){
        $result = +1 * $fop;
        return $result;
        }

      # not
      if( $op eq 'not' ){
        $result = $fop ? 0 : 1;
        return $result if $#$list == -1 || $fop ;
        }

      return 1 if $op eq 'or' && $fop;
      return 0 if $op eq 'and' && !$fop;

      if( $#$list == -1 ){
        &Error("Operational macro $op has less than two arguments:",$$input);
        }
      for( @$list ){
           if( $op eq '+'  ){  $result += $_ }
        elsif( $op eq '-'  ){  $result -= $_ }
        elsif( $op eq '*'  ){  $result *= $_ }
        elsif( $op eq '/'  ){  $result /= $_ }

        elsif( $op eq '='  ){  $result = 1; return 0 unless $fop == $_ }
        elsif( $op eq '!=' ){  $result = 1; return 0 unless $fop != $_ }
        elsif( $op eq '<'  ){  $result = 1; return 0 unless $fop < $_ }
        elsif( $op eq '>'  ){  $result = 1; return 0 unless $fop > $_ }
        elsif( $op eq '<=' ){  $result = 1; return 0 unless $fop <= $_ }
        elsif( $op eq '>=' ){  $result = 1; return 0 unless $fop >= $_ }

        elsif( $op eq 'eq' ){  $result = 1; return 0 unless $fop eq $_ }
        elsif( $op eq 'ne' ){  $result = 1; return 0 unless $fop ne $_ }
        elsif( $op eq 'lt' ){  $result = 1; return 0 unless $fop lt $_ }
        elsif( $op eq 'gt' ){  $result = 1; return 0 unless $fop gt $_ }
        elsif( $op eq 'le' ){  $result = 1; return 0 unless $fop le $_ }
        elsif( $op eq 'ge' ){  $result = 1; return 0 unless $fop ge $_ }

        elsif( $op eq 'and'){  $result = 1; return 0 unless $_ }
        elsif( $op eq 'not'){  $result = 1; return 0 if $_ }
        elsif( $op eq 'or' ){  $result = 0; return 1 if $_ }
        }
      # I do not know when will it be needed, but be consistent
      # a later extension might result a need.
      $result = &DoInput(\$result,$ReferenceFileName,$level+1);
      return $$result;
      }
#Fix by Attila Novak on 13/09/2010
#The version commented out fails due to perl's regex matching limitation
#if the 'value' string following the = in the macro definition is too long
#using index and substr to extract the value fixes this problem
#    if( $$input =~ /^define\s*(?:(\?)\s*|\s+)(\w+)?((?:\:\:\w+)*)((?:.|\n)*?)=((?:.|\n)*)$/ ){
    if( $$input =~ /^define\s*(?:(\?)\s*|\s+)(\w+)?((?:\:\:\w+)*)((?:.|\n)*?)=/ ){
      my $condition = $1;
      my $base = $2;
      my $macro = $3;
      my $parameter = $4;
#      my $value = $5;
      my $value = substr($$input,index($$input,'=')+1);
      $base = $BaseModule unless $base;
      $macro = $base . $macro;
      &DefineMacro($macro,$parameter,$value) unless $condition && defined $Macro{$macro};
      return '';
      }

    if( $$input =~ /^undef\s+(\w+)\s*$/ ){
      delete $Macro{$1};
      delete $MacArg{$1};
      return '';
      }

    if( $$input =~ /^INC\s+(.*)$/ ){
      my $IncludeDirectory = $1;
      $IncludeDirectory =~ s/\s*$//;
      $IncludeDirectory = &Relative($ReferenceFileName,$IncludeDirectory);
      push @INC,$IncludeDirectory;
      return '';
      }

    if( $$input =~ /^restore\s+(\w+)\s*$/ ){
      my $name = $1;
      if( defined( $MacroStack{$name} ) ){
        $MacArg{$name} = pop @{$MacroStack{$name}};
        $Macro{$name}  = pop @{$MacroStack{$name}};
        }else{
        delete $Macro{$1};
        delete $MacArg{$1};
        }
      return '';
      }

    # this is specially to include code into
    # html text
    # This macro would not exists if jamalV1.0 had j-sex
    if( $$input =~ /^include\s+pre\s+\"?(.*?)\"?$/ ){
      my $file = &Relative($ReferenceFileName,$1);
      unless( open(F,"<$file") ){
        &Error("File $file can not be read.",$file);
        exit;
        }
      my $o_irs = $/; undef $/;
      my $f = <F>;
      close F;
      $/ = $o_irs;

      $f =~ s/\</&lt;/g;
      $f =~ s/\>/&gt;/g;
      return $f;
      }

    # include a file as it is without macro processing
    if( $$input =~ /^include\s+verbatim\s+\"?(.*?)\"?$/ ){
      my $file = &Relative($ReferenceFileName,$1);
      unless( open(F,"<$file") ){
        &Error("File $file cann ot be read.",$file);
        exit;
        }
      my $o_irs = $/; undef $/;
      my $f = <F>;
      close F;
      $/ = $o_irs;
      return $f;
      }

    # include file and process macros
    # but do NOT generate output from it
    if( $$input =~ /^include\s+macros?\s+\"?(.*?)\"?$/ ){
      my $file = &Relative($ReferenceFileName,$1);
      &DoFile($file);
      return '';
      }

    # include file and process macros
    if( $$input =~ /^include\s+\"?(.*?)\"?$/ ){
      my $file = &Relative($ReferenceFileName,$1);
      my $output = &DoFile($file);
      return $$output;
      }

    if( $$input =~ /^(?:\[|\{)\s*$/ ){
      return $macroOpen;
      }
    if( $$input =~ /^(?:\]|\})\s*$/ ){
      return $macroClose;
      }

    return &Error("Bad macro.",$$input);

    }elsif( $$input =~ /^(\$(?:\$|\_|\w)+)$/ ){#loop macroes e.g.: {$File$name}
    return $Macro{$1} if $WeAreInLoop;
    return "$macroOpen$$input$macroClose";
    }else{

    # double colon is allowed to separate module macros
    unless( $$input =~ /^(\w+(?:\:\:\w+)*)/ ){
      &Error("Bad format macro:","$macroOpen$$input$macroClose");
      return undef;
      }
    my $name = $1;
    my $args = substr($$input,length($name));
    my $argn = $#{$MacArg{$name}};
    &Warning("Macro needs no argument:","$macroOpen$$input$macroClose")
         if( (!defined($argn) || $argn == -1) && $args !~ /^\s*$/ );
    my $argv = defined($argn) && $argn > -1 ? &Split( $args , $argn+1 ) : [] ;
    my $result = $Macro{$name};
    if( ! defined $result ){
      return &Error("Macro is not defined!",$name) if $ReportNotDefinedMacro;
      return '';
      }

    if( $MacArg{$name} ){# if there are arguments
# Formal argument replacement is going to be quite a complex piece of code.
# The reason for it is that you can use the code
#  {#define X/a/b=ab}
#  {X/baba/kutya}
# then the expected result is 'babakutya' and not 'kutyaakutyaakutya'. This means that the piece of text
# that is generated as the actual value of a formal parameter of the macro
# should not later be parsed for further formal parameters.
#
# In this code an array is built up. Each item contains a piece of string
# and a flag. When an occurence of a formal parameter is found, the string is
# split into three pieces: fragment before the occurence, the occurence, fragment
# after the occurence. The fragment is changed to be the actual value of the formal
# parameter, and the flag says that it should not later be parsed for formal parameters.
# The fragments before and after should still be parsed for formal parameters.
#
      my $resarr = [  { 'frag'=> $result , 'resolv'=> 1 } ];
      for $sear ( @{$MacArg{$name}} ){
        my $repla = shift @{$argv};
        my $nresarr = [];
        for $fragment ( @$resarr ){
          if( $fragment->{'resolv'} ){
            my $item = $fragment->{'frag'};
            while( $item && $item =~ m/(.*?)$sear(.*)/s ){
              push @$nresarr, { 'frag'=> $1, 'resolv'=> 1 } if $1;
              push @$nresarr, { 'frag'=> $repla, 'resolv'=> 0 } if $repla ne '';
              $item = $2;
              }
            push @$nresarr, { 'frag'=> $item, 'resolv'=> 1 } if $item;
            }else{
            push @$nresarr , $fragment;
            }
          }
        $resarr = $nresarr;
        }
      $result = '';
      for $fragment ( @$resarr ){
        $result .= $fragment->{'frag'};
        }
      }
    return $result if $verbatim;
    $result = &DoInput(\$result,$ReferenceFileName,$level+1);
    return $$result;
    }
  }

sub DefineMacro {
  my $name = shift;
  my $args = shift;
  my $value= shift;

  # first store the old value in case it was defined
  # w/o undefing the old value
  if( defined( $Macro{$name} ) ){
    $MacroStack{$name} = [] unless defined $MacroStack{$name};
    push @{$MacroStack{$name}},$Macro{$name};
    push @{$MacroStack{$name}},$MacArg{$name};
    }

  if( length($args) > 0 ){              # there are arguments
    my $argv = &Split($args);           # create formal parameter list
    for( @{$argv} ){ $_ = quotemeta $_ }# make each of them matchable
    $MacArg{$name} = $argv;             # store it
    }else{ $MacArg{$name} = undef }     # but let it be undef if there are no parameters
  $Macro{$name} = $value;               # store the value of the macro

  $value = "$name(" . ( length($args) > 0 ? join(',',@{$argv}):'') . ")=$value";
  &Trace('D',\$value);
  }

#
# split a string into a list
# and return pointer to the list
#
# the second optional parameter gives the
# number of the elements of the list
# Examples:
#
# Split('/13,52/kuk/55')   -> ('13,52','kuk','55')
# Split('/13,52/kuk/55',2) -> ('13,52','kuk/55')
sub Split {
  my $string = shift;
  my $limit = shift;

  $limit = -1 unless defined($limit);

  # get the separator character
  # which is defined by the first
  # character of the string
  my $sep = substr($string,0,1); # the first character
  $string = substr($string,1);   # the rest
  # chop off trailing separator
  while( substr($string,length($string)-1,1) eq $sep){ chop $string; }

  # space but not newline -> separate using multiple spaces
  # any other character   -> separate using the character
  if( $sep =~ /^\s$/ && $sep ne "\n" ){
    $sep = '\s+';
    $string =~ s/^\s*//; # chop off leading spaces if space is the separator
    $string =~ s/\s*$//; # chop off trailing spaces if space is the separator
    }else{ $sep = quotemeta $sep }
  my @list = split($sep,$string,$limit);
  return \@list;
  }

#
# get the start character and
# return the appropriate closing
# character quoted
sub CharPair {
  my $ch = shift;

  if( $ch eq '(' ){ $ch = ')' }
  elsif( $ch eq ')' ){ $ch = '(' }
  elsif( $ch eq '[' ){ $ch = ']' }
  elsif( $ch eq ']' ){ $ch = '[' }
  elsif( $ch eq '{' ){ $ch = '}' }
  elsif( $ch eq '}' ){ $ch = '{' }
  elsif( $ch eq '<' ){ $ch = '>' }
  elsif( $ch eq '>' ){ $ch = '<' }
  return quotemeta $ch;
  }

sub Trace {
  return unless $TraceFile;
  my $type = shift; # one character type: L lines, M macro
  my $lines = shift;
  my $level = shift;
  my $file = $FILE[$#FILE];
  my $line = sprintf("%03d",$LINE{$file});
  my $mo = $macroOpen;

  $level = '-' unless defined $level;

  if( $LastFileNameDisplayedOnTraceOutput ne $file ){
    print TRACE "$line:F:$level: $file:\n";
    $LastFileNameDisplayedOnTraceOutput = $file;
    }

  for( split(/\n/,$$lines ) ){
    if( $type eq 'M' ){
      print TRACE "\n" unless $mo;
      print TRACE "$line:$type:$level: $mo$_";
      $mo = '';
      }
    elsif( $type =~ /L|D/ ){
      print TRACE "$line:$type:$level: $_\n";
      }
    $line++ unless $level && $level ne '-';
    }
  print TRACE "$macroClose\n" if $type eq 'M';
  }

sub Warning {
  return if $SupressWarnings;
  goto &Error;

  }

sub Error {
  my $message = shift;
  my $input = shift;
  my ($pac,$fil,$lin) = caller;

  my $MaxParLength = 20;

  $input = substr($input,0,$MaxParLength) . ' ...' if length($input) > $MaxParLength;
  $input =~ s/\n/\\n/g;

  $message .= "\n";
  $message .= "    Parameter: $input\n" if $input;
  if( $#FILE > -1 ){
    $message .= '    in file ' . $FILE[$#FILE] .' at line ' . $LINE{$FILE[$#FILE]} . "\n";
    }
  $message .= "    source=>$fil , package=>$pac , line=>$lin\n";
  print STDERR $message;
  print TRACE $message if $TraceFile;
  return '' unless $OutputError;
  return "\n*** ERROR ***\n$message*************";
  }

#
# Calculate path to a relative file name
sub Relative {
  my $ReferenceFileName = shift; # the file name with path that is used as reference
  my $RelativeFileName = shift;  # relative file name: relative to the reference file name

  return $RelativeFileName if $RelativeFileName =~ /^\// || # starts with /
                              $RelativeFileName =~ /^\~/ || #             ~/
                              $RelativeFileName =~ /^\\/ || #             \
                              $RelativeFileName =~ /^\w\:/; #             c:
  $RelativeFileName =~ s#^\./##;# allow user to write ./
  if( $ReferenceFileName =~ m{/[^/]+$} ){#if it includes the path
    $ReferenceFileName =~ s{/[^/]+$}{};  #then we now take the file name off
    $ReferenceFileName .= '/';           #but leave the final /
    }else{#if this is only a plain file name taking it off leaves nothing
    $ReferenceFileName = '';
    }
  # $ReferenceFileName now contains only the path
  $RelativeFileName = $ReferenceFileName . $RelativeFileName;
  # just for any sake take off duplicated /
  $RelativeFileName =~ s{//}{/}g;
  # remove all directory/.. form references
  my @dlist = split( /\// , $RelativeFileName );
  my @d = ();
  while( $#dlist > -1 ){
    if( $#dlist == 0 ){
      push @d,$dlist[0];
      last;
      }
    if( $dlist[0] ne '..' && $dlist[1] eq '..' ){
      shift @dlist; shift @dlist;
      }else{ push @d , shift @dlist; }
    }
  $RelativeFileName = join('/',@d);
  return $RelativeFileName; #and now this is not relative anymore
                            # or relative to the cwd
  }

# Predefined macros and other global variables
sub PredefineMacro {

  $WeAreInLoop = 0;
  @LoopMacroStack = ();

  &DefineMacro('version','',$VERSION);
  &DefineMacro('reference','',
"This page was created using <a href=\"http://peter.verhas.com/progs/perl/jamal\">jamal</A> v$VERSION");
  &DefineMacro('timeNow','',$timeNow);

  #global variable containing the names of the moths
  #if you use a different language you can define
  #names in your language using the macro 'months'
  $MonthNames = [
                   'January',
                   'February',
                   'March',
                   'April',
                   'May',
                   'June',
                   'July',
                   'August',
                   'September',
                   'October',
                   'November',
                   'December'
                ];
  #global variable containing the names of the weekdays
  #if you use a different language you can define
  #names in your language using the macro 'days'
  $DayNames = [
              'Monday',
              'Tuesday',
              'Wednesday',
              'Thursday',
              'Friday',
              'Saturday',
              'Sunday'
              ];

  #name to be used in time presentation am and pm
  $AM = 'am'; $PM = 'pm';

  #format of date and time to be presented
  $TimeFormat = 'hh:mm:ss';
  $DateFormat = 'MONTH DD, YEAR.';
  }


sub _convertTimeDate {
  my $time = shift;
  my $format = shift;

  my ($sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst) = localtime($time);
  my $YEAR = 1900 + $year;
  $mon++;
  my $am = $hour < 12 ? $AM : $PM;

  $format =~ s{HH}{$hour}g;
  $hour = '0' . $hour if $hour < 10;
  $format =~ s{0H}{$hour}g;
  $hour -= 12 if $hour > 12;
  $hour =~ s{^0}{}; # some perl version (version 5.005_02 built for sun4-solaris)
                    # resulted leading zeroes here as it was reported by
                    # Sandor Megyaszai megya@cs.tut.fi
                    #                                   June 23, 1999. 22:48
                    # (Fortunately it is not the case for the month names :-)
  $format =~ s{hh}{$hour}g;
  $hour = '0' . $hour if $hour < 10;
  $format =~ s{0h}{$hour}g;

  $format =~ s{am}{$am}g;
  $format =~ s{pm}{$am}g;

  $format =~ s{mm}{$min}g;
  $min = '0' . $min if $min < 10;
  $format =~ s{0m}{$min}g;

  $format =~ s{ss}{$sec}g;
  $sec = '0' . $sec if $sec < 10;
  $format =~ s{0s}{$sec}g;

  $format =~ s{YEAR}{$YEAR}g;
  $format =~ s{YY}{$year}g;

  $format =~ s{MM}{$mon}g;
  $mon = '0' . $mon if $mon < 10;
  $format =~ s{0M}{$mon}g;
  $mon = $MonthNames->[$mon-1];
  $format =~ s{MONTH}{$mon}g;


  $format =~ s{DD}{$mday}g;
  $mday = '0' . $mday if $mday < 10;
  $format =~ s{0D}{$mday}g;

  $wday = 7 unless $wday;
  $format =~ s{WD}{$wday}g;
  $wday = $DayNames->[$wday-1];
  $format =~ s{DAY}{$wday}g;

  return $format;
  }
sub ConvertDate {
  &_convertTimeDate($_[0],$DateFormat);
  }
sub ConvertTime {
  &_convertTimeDate($_[0],$TimeFormat);
  }

# simple count new lines in a string
sub CNL { return $_[0] =~ tr/\n/\n/ }

sub HtmlParameters {
  my $file = shift;

  open(F,"<$file");
  my $o_irs = $/; undef $/;
  $file = <F>;
  close F;
  $/ = $o_irs;

  $file =~ m{<title>(.*?)</title>}i;
  my $title = $1;
  return ($title);
  }

sub help {
  print <<END;
jamal html preprocessor V$VERSION
Usage:
.
jamal [-options] input [output]
.
Options:
-? or -h               help
-m file                macro definition file
-D                     macro define
-I                     j-SEX directory
-0                     suppress output
-d                     put error message into result
-t file                trace file
.
Your default extension directories are:
END
  for( @INC ){
    print "$_\n";
    }
  }

=pod
=section maked
=H Create a subdirectory
Usage

=verbatim
           &maked($directory)
=noverbatim
Hierarchically creates the directory T<$directory>.
If directory already exists, it does not do anything.

The parameter T<$directory> should contain the trailing file name.

This function can be used before outputting to a new file to be
sure that the directory already exists.
=cut
sub maked {
  my $directory = shift;
  my $root;

  $directory =~ s{\\}{/}g; #make the file name UNIX compliant under Windows NT
                           #this should not hurt under UNIX unless you are
                           #pervert to include a \ in a file name.
  my(@d) = split(/\//,$directory);

  pop @d; #pop off the file name

  if( $d[0] =~ s/^(\w:)// ){
    shift @d if $d[0] eq '';
    unshift @d , $1;
    }

  if( $#d == -1 ){ return; }#this is the root directory

  if( $d[0] =~ /^\w:$/ ){#drive letter under Windows
    $root = shift @d;
    }elsif( $directory =~ /^\//){
    $root = '/';
    }else{
    $root = '';
    }

  for( @d ){
    $root .= '/' if $root;           # add a separator if there is something to separate
    $root .= $_;                     # take the next subdirectory
    -d $root || mkdir $root,$umask;  # it exists or create the directory
    }
  return 1; # we are done, and fine
  }


=pod
=section packageJamal
=H Writing extensions for jamal in Perl
=abstract
The package that jamal does not use but provides an interface for
extension modules. Calling some of these functions is mandatory!
Functions below this chapter define the interface.
=end
The Perl extensions that can process Perl defined macros can
use sevelar functions that are all put into the package T<jamal>.
Some of the functions provide possibilities for the extension modules,
others should be called.

The rules for a module to be jamal compliant
=itemize
=item call function T<&jamal::require> (see R<jamalrequire>) to specify the version
of jamal the module can work with
=item call function T<&jamal::version> (see R<jamalversion>) to specify the version
interval of the module it is compatible with
=item call functions from the package jamal
=item do NOT directly call functions or access any entity from the package main.
=noitemize

Modification of the jamal code is bad practice. Distribution of such a code
is highly discouraged.

=cut
package jamal;


=pod
=section jamalDefineMacro
=H Define package specific macro

This function can be called from the Perl macro extension packages.
Calling this function helps to define package specific user defined
macros. The caller should call this function, like

=verbatim
           &jamal::DefineMacro( 'macroName' , ',argument,list','macro body');
=noverbatim

This function in fact calls the T<DefineMacro> of the package T<main>, however
prepends the package name before the name of the macro. Therefore the above
function call made from package e.g.: T<macpac> is equivalent to:
=verbatim
           &main::DefineMacro( 'macpac::macroName' , ',argument,list','macro body');
=noverbatim

Note that the argument list is a string and should start with the separator character,
even if there is only one argument.

Example:
=verbatim
           &jamal::DefineMacro('cap',',X','<FONT SIZE=+1>X</FONT>');
=noverbatim
Note that there is a comma preceeding the parameter T<X>

=cut
sub DefineMacro {
  my @cal = caller;
  my $caller = substr( $cal[0],7);
  &main::DefineMacro("$caller" . '::' . $_[0] , $_[1], $_[2] );
  }

=pod
=section jamalSplit
=H Call the argument split function

This is just a stub calling T<main::Split>.

You can call this function if you have a jamal parameter list that
you want to split into an array. There are two arguments. The first argument
is a string containing the jamal parameter list. This is a list separated
by some character. The very first character of the string is used as a separator
character. If you want to use the comma as a separator character
you have to put a comma before the first list element. In other words the
list separator character not only separates but much rather preceedes each
element of the list.

The second argument is optional and can define the number of the elements in
the list. If the number is not defined there will be so many elements of the
list as many times the separator character appears in the string. If the
number T<n> is defined splitting stops after the T<n>th element. In other words
the last element of the list can contain the list separator.

Examples:
=verbatim
       &jamal::Split('/13,52/kuk/55')   -> ['13,52','kuk','55']
       &jamal::Split('/13,52/kuk/55',2) -> ['13,52','kuk/55']
=noverbatim

The function returns a pointer to the resulting list and NOT the list itself.

=cut
sub Split {
  goto &main::Split;
  }
=pod
=section jamalOpen
=H Macro open string

Returns the actual macro open string.
=cut
sub Open  { $main::macroOpen  }
=pod
=section jamalClose
=H Macro close string

Returns the actual macro close string.
=cut
sub Close { $main::macroClose }

=pod
=section jamalversion
=H Requested module version

Using this function the module can set the module version number
as well as retrieve the requested version.

=bold
This is regarding the version of the module and NOT the version of jamal.
=nobold

Based on the requested version the module can behave differently,
therefore backward compatibility can be maintained.

On the other hand, if backward compatibility is not maintained
down to all versions a minimal as well as maximal version number
can be specified. If the T<use> statement requires an older or
newer version jamal will generate a fatal error.

Usage:
=verbatim
            &jamal::version
=noverbatim

returns the version that the user requested after the name of the
package in the macro T<use>.

=verbatim
           &jamal::version('1.0','3.12');
=noverbatim

tells jamal that the module implementation is comatible down to version 1.0
and the current version is 3.12 (the numbers here are only examples). If one
requests version 0.98 or 3.3 will get a fatal error.
=cut
sub version {
  $VersionWasCalled = 1;
  my $minV = shift;
  my $maxV = shift;
  my @cal = caller;
  my $caller = substr( $cal[0],7);
  $main::minV{$caller} = $minV if defined $minV;
  $main::maxV{$caller} = $maxV if defined $maxV;
  return $main::Module{$caller};
  }

=pod
=section jamalverbatim
=H Tell jamal that the result of the macro should be verbatim

A macro extension can call this function to tell jamal that the result
of the macro should be treated as is, without futrher processing.

This function can be called with or without argument. Usage:

=verbatim
              &jamal::verbatim
              &jamal::verbatim(1)
              &jamal::verbatim(0)
=noverbatim

The first two callings are equivalent and tell jamal that the result should
not be searched for further embedded macros. The third version tells jamal to
process the result for embedded macros. This bahaviour is the default, so
there is no actual need to make such a function call. In some cases it can be
convenient to use the third format in case the function has already called T<verbatim>

but later it changes it'smind.

The first format is the suggested proper use.

Note that calling this function does not tell jamal that the specific macro
output should always be treated as verbatim. This only tells jamal that the
current result is verbatim. Therefore the function should be called each time
the macro function is called. See example in example.pm

=cut
sub verbatim {
  my $value = shift;
  $value = 1 unless defined $vlaue;
  $main::ExtensionVerbatim = $value;
  }

=pod
=section jamalinput
=H Convert relative input file name to absolute

A macro extension can call this functions to convert a file name,
which is probably given to the macro as argument, from relative
to absolute.

For example jamal was invoked as

=verbatim
        perl jamal.pl test/test.jam output/test.html
=noverbatim

then
=verbatim
       &jamal::input('incfil.jam')
=noverbatim

will return

=verbatim
       test/incfil.jam
=noverbatim

which is a file name that the module can use in Perl operators like T<open>.

This function leaves the absolute file names intact.
=cut
sub input {
  return &main::Relative($main::InputFileName,$_[0]);
  }

=pod
=section jamaloutput
=H Convert relative output file name to absolute

A macro extension can call this functions to convert a file name,
which is probably given to the macro as argument, from relative
to absolute.

For example jamal was invoked as

=verbatim
        perl jamal.pl test/test.jam output/test.html
=noverbatim

then
=verbatim
       &jamal::output('incfil.html')
=noverbatim

will return

=verbatim
       output/incfil.htm
=noverbatim

which is a file name that the module can use in Perl operators like T<open>.

This function leaves the absolute file names intact.
=cut
sub output {
  return &main::Relative($main::OutputFileName,$_[0]) if $main::OutputFileName;
  #if the output is put to screen
  return &main::Relative($main::InputFileName,$_[0]);
  }

=pod
=section jamalrequire
=H Require jamal version

This function should be called in the initialization code (subroutine T<BEGIN>)
of the module to tell jamal that the module needs at least this high version
of jamal.

Usage:
=verbatim
             &jamal::require('2.0')
=noverbatim

The argument is the version required. '2.0' is the absolute minimum as versions
below did not implement Perl written extensions. Therefore requiring version 2.0
seems to be useless, but it is B<NOT>.

=bold
All Perl written extensions should call this function to require the version it needs.
=nobold

The reason: jamal version X, which is in the future might define the Perl witten
extension interface a bit differently than version Y (X>Y). However it will serve
the same interface and behave the same way for modules requiring version Y.

Now think of Y=2.0 and do not make the same mistake as me not implementing the user
macro T<require> in jamal version 1.0

=cut
sub require{
  $RequireWasCalled = 1;
  my $rversion = shift;
  my $caller = substr( $cal[0],7);
  if( $rversion > $main::VERSION ){
    &main::Error("Module $caller requires jamal V$rversion and I am only $main::VERSION",undef);
    exit;
    }
  # this is not used in jamal V2.0, may be later versions
  # will use this to mimic different interfaces for different modules
  # for backward compatibility (in case I define some interface
  # absolutely wrong)
  $main::ModRequire{$caller} = $rversion;
  }
=pod
=section jamalError
=H Display warning or error

Calling this function from a Perl extension module creates
warning or error message.

It is the caller responsibility to execute an exit after calling
T<Error> in case the program think that the error is unrecoverable.

Usage:
=verbatim
              &jamal::Warning('I think there might be some slight problem');

              or

              &jamal::Error('I can not cope with you!'); exit;

              or

              &jamal::Error('This is a serious error, but I keep trying.'); #no exit
=noverbatim
=cut
sub Warning {
  return if $main::SupressWarnings;
  goto &Error;

  }

sub Error {
  my $message = shift;
  my $input = shift;
  my ($pac,$fil,$lin) = caller;

  my $MaxParLength = 20;

  $input = substr($input,0,$MaxParLength) . ' ...' if length($input) > $MaxParLength;

  print STDERR "$message\n";
  print STDERR "    Parameter: $input\n" if $input;
  print STDERR "    in file $fil at line $lin\n";
  }

__END__