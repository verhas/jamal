package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestCounter {

    @Test
    @DisplayName("Counter works")
    void testCounter() throws Exception {
        TestThat.theInput("{@counter:define id=f}{f}. {f}. {f}. {f}. {f}. {f last}. {f}."
        ).results("1. 2. 3. 4. 5. 5. 6.");
    }

    @Test
    @DisplayName("Test save")
    void testCounter1() throws Exception {
        TestThat.theInput("{@counter:define id=f}{f -> aba}. {aba}. {f}. {f}. {f}. {f last}. {f}."
        ).results("1. 1. 2. 3. 4. 4. 5.");
    }

    @Test
    @DisplayName("Counter global works")
    void testCounterGlobal() throws Exception {
        TestThat.theInput("{@counter:define id=:f}{f}. {f}. {f}. {f}. {f}. {f}."
        ).results("1. 2. 3. 4. 5. 6.");
    }

    @Test
    @DisplayName("Bad format throws exception")
    void testBadFormat() throws Exception {
        TestThat.theInput("{@counter:define id=:f format=%f}{f}. {f}. {f}. {f}. {f}. {f}."
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Bad counter definition ")
    void testBadCounterdef() throws Exception {
        TestThat.theInput("{@counter:define id=:f\nsdsd}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Start and step can be defined")
    void testStartStep() throws Exception {
        TestThat.theInput("{#counter:define {@define start=3}{@define step=2}id=f}{f}. {f}. {f}. {f}. {f}. {f}."
        ).results("3. 5. 7. 9. 11. 13.");
        TestThat.theInput("{#counter:define start=3 step=2 id=f}{f}. {f}. {f}. {f}. {f}. {f}."
        ).results("3. 5. 7. 9. 11. 13.");
    }

    @Test
    @DisplayName("Start and step can be defined")
    void testStartStepAsParam() throws Exception {
        TestThat.theInput("{#counter:define start=3 step=2 id=f}{f}. {f}. {f}. {f}. {f}. {f}."
        ).results("3. 5. 7. 9. 11. 13.");
    }

    @Test
    @DisplayName("Text after the identifier is error")
    void testIgnoredText() throws Exception {
        TestThat.theInput("{@counter:define id=f this text is not ignored}{f}. {f}. {f}."
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("Format can be defined as user defined macro")
    void testFormatCanBeDefined() throws Exception {
        TestThat.theInput("{#counter:define {@define format=%03d.}id=f}{f} {f} {f}"
        ).results("001. 002. 003.");
    }

    @Test
    @DisplayName("Alpha format can be defined as user defined macro")
    void testAlphaFormatCanBeDefined() throws Exception {
        TestThat.theInput("{#counter:define {@define format=$alpha.)}id=f}{f} {f} {f}"
        ).results("a.) b.) c.)");
    }

    @Test
    @DisplayName("ALPHA format can be defined as user defined macro")
    void testALPHAFormatCanBeDefined() throws Exception {
        TestThat.theInput("{#counter:define {@define format=$ALPHA.)}id=f}{f} {f} {f}"
        ).results("A.) B.) C.)");
    }

    @Test
    @DisplayName("Alpha format can run out of letters")
    void testAlphaCanRunOutOfLetters() throws Exception {
        TestThat.theInput("{#counter:define {@define start=26}{@define format=$ALPHA.)}id=f}{f}"
        ).results("Z.)");
        TestThat.theInput("{#counter:define {@define start=27}{@define format=$ALPHA.)}id=f}{f}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("ROMAN format can be defined as user defined macro")
    void testROMANFormatCanBeDefined() throws Exception {
        TestThat.theInput("{#counter:define {@define format=$ROMAN.)}id=f}{f} {f} {f}"
        ).results("I.) II.) III.)");
    }

    @Test
    @DisplayName("The option `IIII` will create a different 4")
    void testROMANFormatIIII() throws Exception {
        TestThat.theInput("{#counter:define IIII format=$ROMAN.) id=f}{f} {f} {f} {f}"
        ).results("I.) II.) III.) IIII.)");
    }

    @Test
    @DisplayName("Roman format can be defined as user defined macro")
    void testRomanFormatCanBeDefined() throws Exception {
        TestThat.theInput("{#counter:define {@define format=$roman.)}id=f}{f} {f} {f}"
        ).results("i.) ii.) iii.)");
    }

    @Test
    @DisplayName("Roman format can run out of letters")
    void testRomanCanRunOutOfLetters() throws Exception {
        TestThat.theInput("{#counter:define {@define start=3999}{@define format=$roman}id=f}{f}"
        ).results("mmmcmxcix");
        TestThat.theInput("{#counter:define {@define start=4000}{@define format=$roman}id=f}{f}"
        ).throwsBadSyntax();
    }

    @Test
    @DisplayName("All roman format work")
    void testRomanFormatAll() throws Exception {
        TestThat.theInput("{#counter:define {@define format=%d=$roman}id=f}" +
                "{f} {f} {f} {f} {f} {f} {f} {f} {f} {f} \n".repeat(399) +
                "{f} ".repeat(9)
        ).results(
                //<editor-fold desc="all the roman numerals from 1=i to 3999=mmmcmxcix in a string">
                "1=i 2=ii 3=iii 4=iv 5=v 6=vi 7=vii 8=viii 9=ix 10=x \n" +
                        "11=xi 12=xii 13=xiii 14=xiv 15=xv 16=xvi 17=xvii 18=xviii 19=xix 20=xx \n" +
                        "21=xxi 22=xxii 23=xxiii 24=xxiv 25=xxv 26=xxvi 27=xxvii 28=xxviii 29=xxix 30=xxx \n" +
                        "31=xxxi 32=xxxii 33=xxxiii 34=xxxiv 35=xxxv 36=xxxvi 37=xxxvii 38=xxxviii 39=xxxix 40=xl \n" +
                        "41=xli 42=xlii 43=xliii 44=xliv 45=xlv 46=xlvi 47=xlvii 48=xlviii 49=xlix 50=l \n" +
                        "51=li 52=lii 53=liii 54=liv 55=lv 56=lvi 57=lvii 58=lviii 59=lix 60=lx \n" +
                        "61=lxi 62=lxii 63=lxiii 64=lxiv 65=lxv 66=lxvi 67=lxvii 68=lxviii 69=lxix 70=lxx \n" +
                        "71=lxxi 72=lxxii 73=lxxiii 74=lxxiv 75=lxxv 76=lxxvi 77=lxxvii 78=lxxviii 79=lxxix 80=lxxx \n" +
                        "81=lxxxi 82=lxxxii 83=lxxxiii 84=lxxxiv 85=lxxxv 86=lxxxvi 87=lxxxvii 88=lxxxviii 89=lxxxix 90=xc \n" +
                        "91=xci 92=xcii 93=xciii 94=xciv 95=xcv 96=xcvi 97=xcvii 98=xcviii 99=xcix 100=c \n" +
                        "101=ci 102=cii 103=ciii 104=civ 105=cv 106=cvi 107=cvii 108=cviii 109=cix 110=cx \n" +
                        "111=cxi 112=cxii 113=cxiii 114=cxiv 115=cxv 116=cxvi 117=cxvii 118=cxviii 119=cxix 120=cxx \n" +
                        "121=cxxi 122=cxxii 123=cxxiii 124=cxxiv 125=cxxv 126=cxxvi 127=cxxvii 128=cxxviii 129=cxxix 130=cxxx \n" +
                        "131=cxxxi 132=cxxxii 133=cxxxiii 134=cxxxiv 135=cxxxv 136=cxxxvi 137=cxxxvii 138=cxxxviii 139=cxxxix 140=cxl \n" +
                        "141=cxli 142=cxlii 143=cxliii 144=cxliv 145=cxlv 146=cxlvi 147=cxlvii 148=cxlviii 149=cxlix 150=cl \n" +
                        "151=cli 152=clii 153=cliii 154=cliv 155=clv 156=clvi 157=clvii 158=clviii 159=clix 160=clx \n" +
                        "161=clxi 162=clxii 163=clxiii 164=clxiv 165=clxv 166=clxvi 167=clxvii 168=clxviii 169=clxix 170=clxx \n" +
                        "171=clxxi 172=clxxii 173=clxxiii 174=clxxiv 175=clxxv 176=clxxvi 177=clxxvii 178=clxxviii 179=clxxix 180=clxxx \n" +
                        "181=clxxxi 182=clxxxii 183=clxxxiii 184=clxxxiv 185=clxxxv 186=clxxxvi 187=clxxxvii 188=clxxxviii 189=clxxxix 190=cxc \n" +
                        "191=cxci 192=cxcii 193=cxciii 194=cxciv 195=cxcv 196=cxcvi 197=cxcvii 198=cxcviii 199=cxcix 200=cc \n" +
                        "201=cci 202=ccii 203=cciii 204=cciv 205=ccv 206=ccvi 207=ccvii 208=ccviii 209=ccix 210=ccx \n" +
                        "211=ccxi 212=ccxii 213=ccxiii 214=ccxiv 215=ccxv 216=ccxvi 217=ccxvii 218=ccxviii 219=ccxix 220=ccxx \n" +
                        "221=ccxxi 222=ccxxii 223=ccxxiii 224=ccxxiv 225=ccxxv 226=ccxxvi 227=ccxxvii 228=ccxxviii 229=ccxxix 230=ccxxx \n" +
                        "231=ccxxxi 232=ccxxxii 233=ccxxxiii 234=ccxxxiv 235=ccxxxv 236=ccxxxvi 237=ccxxxvii 238=ccxxxviii 239=ccxxxix 240=ccxl \n" +
                        "241=ccxli 242=ccxlii 243=ccxliii 244=ccxliv 245=ccxlv 246=ccxlvi 247=ccxlvii 248=ccxlviii 249=ccxlix 250=ccl \n" +
                        "251=ccli 252=cclii 253=ccliii 254=ccliv 255=cclv 256=cclvi 257=cclvii 258=cclviii 259=cclix 260=cclx \n" +
                        "261=cclxi 262=cclxii 263=cclxiii 264=cclxiv 265=cclxv 266=cclxvi 267=cclxvii 268=cclxviii 269=cclxix 270=cclxx \n" +
                        "271=cclxxi 272=cclxxii 273=cclxxiii 274=cclxxiv 275=cclxxv 276=cclxxvi 277=cclxxvii 278=cclxxviii 279=cclxxix 280=cclxxx \n" +
                        "281=cclxxxi 282=cclxxxii 283=cclxxxiii 284=cclxxxiv 285=cclxxxv 286=cclxxxvi 287=cclxxxvii 288=cclxxxviii 289=cclxxxix 290=ccxc \n" +
                        "291=ccxci 292=ccxcii 293=ccxciii 294=ccxciv 295=ccxcv 296=ccxcvi 297=ccxcvii 298=ccxcviii 299=ccxcix 300=ccc \n" +
                        "301=ccci 302=cccii 303=ccciii 304=ccciv 305=cccv 306=cccvi 307=cccvii 308=cccviii 309=cccix 310=cccx \n" +
                        "311=cccxi 312=cccxii 313=cccxiii 314=cccxiv 315=cccxv 316=cccxvi 317=cccxvii 318=cccxviii 319=cccxix 320=cccxx \n" +
                        "321=cccxxi 322=cccxxii 323=cccxxiii 324=cccxxiv 325=cccxxv 326=cccxxvi 327=cccxxvii 328=cccxxviii 329=cccxxix 330=cccxxx \n" +
                        "331=cccxxxi 332=cccxxxii 333=cccxxxiii 334=cccxxxiv 335=cccxxxv 336=cccxxxvi 337=cccxxxvii 338=cccxxxviii 339=cccxxxix 340=cccxl \n" +
                        "341=cccxli 342=cccxlii 343=cccxliii 344=cccxliv 345=cccxlv 346=cccxlvi 347=cccxlvii 348=cccxlviii 349=cccxlix 350=cccl \n" +
                        "351=cccli 352=ccclii 353=cccliii 354=cccliv 355=ccclv 356=ccclvi 357=ccclvii 358=ccclviii 359=ccclix 360=ccclx \n" +
                        "361=ccclxi 362=ccclxii 363=ccclxiii 364=ccclxiv 365=ccclxv 366=ccclxvi 367=ccclxvii 368=ccclxviii 369=ccclxix 370=ccclxx \n" +
                        "371=ccclxxi 372=ccclxxii 373=ccclxxiii 374=ccclxxiv 375=ccclxxv 376=ccclxxvi 377=ccclxxvii 378=ccclxxviii 379=ccclxxix 380=ccclxxx \n" +
                        "381=ccclxxxi 382=ccclxxxii 383=ccclxxxiii 384=ccclxxxiv 385=ccclxxxv 386=ccclxxxvi 387=ccclxxxvii 388=ccclxxxviii 389=ccclxxxix 390=cccxc \n" +
                        "391=cccxci 392=cccxcii 393=cccxciii 394=cccxciv 395=cccxcv 396=cccxcvi 397=cccxcvii 398=cccxcviii 399=cccxcix 400=cd \n" +
                        "401=cdi 402=cdii 403=cdiii 404=cdiv 405=cdv 406=cdvi 407=cdvii 408=cdviii 409=cdix 410=cdx \n" +
                        "411=cdxi 412=cdxii 413=cdxiii 414=cdxiv 415=cdxv 416=cdxvi 417=cdxvii 418=cdxviii 419=cdxix 420=cdxx \n" +
                        "421=cdxxi 422=cdxxii 423=cdxxiii 424=cdxxiv 425=cdxxv 426=cdxxvi 427=cdxxvii 428=cdxxviii 429=cdxxix 430=cdxxx \n" +
                        "431=cdxxxi 432=cdxxxii 433=cdxxxiii 434=cdxxxiv 435=cdxxxv 436=cdxxxvi 437=cdxxxvii 438=cdxxxviii 439=cdxxxix 440=cdxl \n" +
                        "441=cdxli 442=cdxlii 443=cdxliii 444=cdxliv 445=cdxlv 446=cdxlvi 447=cdxlvii 448=cdxlviii 449=cdxlix 450=cdl \n" +
                        "451=cdli 452=cdlii 453=cdliii 454=cdliv 455=cdlv 456=cdlvi 457=cdlvii 458=cdlviii 459=cdlix 460=cdlx \n" +
                        "461=cdlxi 462=cdlxii 463=cdlxiii 464=cdlxiv 465=cdlxv 466=cdlxvi 467=cdlxvii 468=cdlxviii 469=cdlxix 470=cdlxx \n" +
                        "471=cdlxxi 472=cdlxxii 473=cdlxxiii 474=cdlxxiv 475=cdlxxv 476=cdlxxvi 477=cdlxxvii 478=cdlxxviii 479=cdlxxix 480=cdlxxx \n" +
                        "481=cdlxxxi 482=cdlxxxii 483=cdlxxxiii 484=cdlxxxiv 485=cdlxxxv 486=cdlxxxvi 487=cdlxxxvii 488=cdlxxxviii 489=cdlxxxix 490=cdxc \n" +
                        "491=cdxci 492=cdxcii 493=cdxciii 494=cdxciv 495=cdxcv 496=cdxcvi 497=cdxcvii 498=cdxcviii 499=cdxcix 500=d \n" +
                        "501=di 502=dii 503=diii 504=div 505=dv 506=dvi 507=dvii 508=dviii 509=dix 510=dx \n" +
                        "511=dxi 512=dxii 513=dxiii 514=dxiv 515=dxv 516=dxvi 517=dxvii 518=dxviii 519=dxix 520=dxx \n" +
                        "521=dxxi 522=dxxii 523=dxxiii 524=dxxiv 525=dxxv 526=dxxvi 527=dxxvii 528=dxxviii 529=dxxix 530=dxxx \n" +
                        "531=dxxxi 532=dxxxii 533=dxxxiii 534=dxxxiv 535=dxxxv 536=dxxxvi 537=dxxxvii 538=dxxxviii 539=dxxxix 540=dxl \n" +
                        "541=dxli 542=dxlii 543=dxliii 544=dxliv 545=dxlv 546=dxlvi 547=dxlvii 548=dxlviii 549=dxlix 550=dl \n" +
                        "551=dli 552=dlii 553=dliii 554=dliv 555=dlv 556=dlvi 557=dlvii 558=dlviii 559=dlix 560=dlx \n" +
                        "561=dlxi 562=dlxii 563=dlxiii 564=dlxiv 565=dlxv 566=dlxvi 567=dlxvii 568=dlxviii 569=dlxix 570=dlxx \n" +
                        "571=dlxxi 572=dlxxii 573=dlxxiii 574=dlxxiv 575=dlxxv 576=dlxxvi 577=dlxxvii 578=dlxxviii 579=dlxxix 580=dlxxx \n" +
                        "581=dlxxxi 582=dlxxxii 583=dlxxxiii 584=dlxxxiv 585=dlxxxv 586=dlxxxvi 587=dlxxxvii 588=dlxxxviii 589=dlxxxix 590=dxc \n" +
                        "591=dxci 592=dxcii 593=dxciii 594=dxciv 595=dxcv 596=dxcvi 597=dxcvii 598=dxcviii 599=dxcix 600=dc \n" +
                        "601=dci 602=dcii 603=dciii 604=dciv 605=dcv 606=dcvi 607=dcvii 608=dcviii 609=dcix 610=dcx \n" +
                        "611=dcxi 612=dcxii 613=dcxiii 614=dcxiv 615=dcxv 616=dcxvi 617=dcxvii 618=dcxviii 619=dcxix 620=dcxx \n" +
                        "621=dcxxi 622=dcxxii 623=dcxxiii 624=dcxxiv 625=dcxxv 626=dcxxvi 627=dcxxvii 628=dcxxviii 629=dcxxix 630=dcxxx \n" +
                        "631=dcxxxi 632=dcxxxii 633=dcxxxiii 634=dcxxxiv 635=dcxxxv 636=dcxxxvi 637=dcxxxvii 638=dcxxxviii 639=dcxxxix 640=dcxl \n" +
                        "641=dcxli 642=dcxlii 643=dcxliii 644=dcxliv 645=dcxlv 646=dcxlvi 647=dcxlvii 648=dcxlviii 649=dcxlix 650=dcl \n" +
                        "651=dcli 652=dclii 653=dcliii 654=dcliv 655=dclv 656=dclvi 657=dclvii 658=dclviii 659=dclix 660=dclx \n" +
                        "661=dclxi 662=dclxii 663=dclxiii 664=dclxiv 665=dclxv 666=dclxvi 667=dclxvii 668=dclxviii 669=dclxix 670=dclxx \n" +
                        "671=dclxxi 672=dclxxii 673=dclxxiii 674=dclxxiv 675=dclxxv 676=dclxxvi 677=dclxxvii 678=dclxxviii 679=dclxxix 680=dclxxx \n" +
                        "681=dclxxxi 682=dclxxxii 683=dclxxxiii 684=dclxxxiv 685=dclxxxv 686=dclxxxvi 687=dclxxxvii 688=dclxxxviii 689=dclxxxix 690=dcxc \n" +
                        "691=dcxci 692=dcxcii 693=dcxciii 694=dcxciv 695=dcxcv 696=dcxcvi 697=dcxcvii 698=dcxcviii 699=dcxcix 700=dcc \n" +
                        "701=dcci 702=dccii 703=dcciii 704=dcciv 705=dccv 706=dccvi 707=dccvii 708=dccviii 709=dccix 710=dccx \n" +
                        "711=dccxi 712=dccxii 713=dccxiii 714=dccxiv 715=dccxv 716=dccxvi 717=dccxvii 718=dccxviii 719=dccxix 720=dccxx \n" +
                        "721=dccxxi 722=dccxxii 723=dccxxiii 724=dccxxiv 725=dccxxv 726=dccxxvi 727=dccxxvii 728=dccxxviii 729=dccxxix 730=dccxxx \n" +
                        "731=dccxxxi 732=dccxxxii 733=dccxxxiii 734=dccxxxiv 735=dccxxxv 736=dccxxxvi 737=dccxxxvii 738=dccxxxviii 739=dccxxxix 740=dccxl \n" +
                        "741=dccxli 742=dccxlii 743=dccxliii 744=dccxliv 745=dccxlv 746=dccxlvi 747=dccxlvii 748=dccxlviii 749=dccxlix 750=dccl \n" +
                        "751=dccli 752=dcclii 753=dccliii 754=dccliv 755=dcclv 756=dcclvi 757=dcclvii 758=dcclviii 759=dcclix 760=dcclx \n" +
                        "761=dcclxi 762=dcclxii 763=dcclxiii 764=dcclxiv 765=dcclxv 766=dcclxvi 767=dcclxvii 768=dcclxviii 769=dcclxix 770=dcclxx \n" +
                        "771=dcclxxi 772=dcclxxii 773=dcclxxiii 774=dcclxxiv 775=dcclxxv 776=dcclxxvi 777=dcclxxvii 778=dcclxxviii 779=dcclxxix 780=dcclxxx \n" +
                        "781=dcclxxxi 782=dcclxxxii 783=dcclxxxiii 784=dcclxxxiv 785=dcclxxxv 786=dcclxxxvi 787=dcclxxxvii 788=dcclxxxviii 789=dcclxxxix 790=dccxc \n" +
                        "791=dccxci 792=dccxcii 793=dccxciii 794=dccxciv 795=dccxcv 796=dccxcvi 797=dccxcvii 798=dccxcviii 799=dccxcix 800=dccc \n" +
                        "801=dccci 802=dcccii 803=dccciii 804=dccciv 805=dcccv 806=dcccvi 807=dcccvii 808=dcccviii 809=dcccix 810=dcccx \n" +
                        "811=dcccxi 812=dcccxii 813=dcccxiii 814=dcccxiv 815=dcccxv 816=dcccxvi 817=dcccxvii 818=dcccxviii 819=dcccxix 820=dcccxx \n" +
                        "821=dcccxxi 822=dcccxxii 823=dcccxxiii 824=dcccxxiv 825=dcccxxv 826=dcccxxvi 827=dcccxxvii 828=dcccxxviii 829=dcccxxix 830=dcccxxx \n" +
                        "831=dcccxxxi 832=dcccxxxii 833=dcccxxxiii 834=dcccxxxiv 835=dcccxxxv 836=dcccxxxvi 837=dcccxxxvii 838=dcccxxxviii 839=dcccxxxix 840=dcccxl \n" +
                        "841=dcccxli 842=dcccxlii 843=dcccxliii 844=dcccxliv 845=dcccxlv 846=dcccxlvi 847=dcccxlvii 848=dcccxlviii 849=dcccxlix 850=dcccl \n" +
                        "851=dcccli 852=dccclii 853=dcccliii 854=dcccliv 855=dccclv 856=dccclvi 857=dccclvii 858=dccclviii 859=dccclix 860=dccclx \n" +
                        "861=dccclxi 862=dccclxii 863=dccclxiii 864=dccclxiv 865=dccclxv 866=dccclxvi 867=dccclxvii 868=dccclxviii 869=dccclxix 870=dccclxx \n" +
                        "871=dccclxxi 872=dccclxxii 873=dccclxxiii 874=dccclxxiv 875=dccclxxv 876=dccclxxvi 877=dccclxxvii 878=dccclxxviii 879=dccclxxix 880=dccclxxx \n" +
                        "881=dccclxxxi 882=dccclxxxii 883=dccclxxxiii 884=dccclxxxiv 885=dccclxxxv 886=dccclxxxvi 887=dccclxxxvii 888=dccclxxxviii 889=dccclxxxix 890=dcccxc \n" +
                        "891=dcccxci 892=dcccxcii 893=dcccxciii 894=dcccxciv 895=dcccxcv 896=dcccxcvi 897=dcccxcvii 898=dcccxcviii 899=dcccxcix 900=cm \n" +
                        "901=cmi 902=cmii 903=cmiii 904=cmiv 905=cmv 906=cmvi 907=cmvii 908=cmviii 909=cmix 910=cmx \n" +
                        "911=cmxi 912=cmxii 913=cmxiii 914=cmxiv 915=cmxv 916=cmxvi 917=cmxvii 918=cmxviii 919=cmxix 920=cmxx \n" +
                        "921=cmxxi 922=cmxxii 923=cmxxiii 924=cmxxiv 925=cmxxv 926=cmxxvi 927=cmxxvii 928=cmxxviii 929=cmxxix 930=cmxxx \n" +
                        "931=cmxxxi 932=cmxxxii 933=cmxxxiii 934=cmxxxiv 935=cmxxxv 936=cmxxxvi 937=cmxxxvii 938=cmxxxviii 939=cmxxxix 940=cmxl \n" +
                        "941=cmxli 942=cmxlii 943=cmxliii 944=cmxliv 945=cmxlv 946=cmxlvi 947=cmxlvii 948=cmxlviii 949=cmxlix 950=cml \n" +
                        "951=cmli 952=cmlii 953=cmliii 954=cmliv 955=cmlv 956=cmlvi 957=cmlvii 958=cmlviii 959=cmlix 960=cmlx \n" +
                        "961=cmlxi 962=cmlxii 963=cmlxiii 964=cmlxiv 965=cmlxv 966=cmlxvi 967=cmlxvii 968=cmlxviii 969=cmlxix 970=cmlxx \n" +
                        "971=cmlxxi 972=cmlxxii 973=cmlxxiii 974=cmlxxiv 975=cmlxxv 976=cmlxxvi 977=cmlxxvii 978=cmlxxviii 979=cmlxxix 980=cmlxxx \n" +
                        "981=cmlxxxi 982=cmlxxxii 983=cmlxxxiii 984=cmlxxxiv 985=cmlxxxv 986=cmlxxxvi 987=cmlxxxvii 988=cmlxxxviii 989=cmlxxxix 990=cmxc \n" +
                        "991=cmxci 992=cmxcii 993=cmxciii 994=cmxciv 995=cmxcv 996=cmxcvi 997=cmxcvii 998=cmxcviii 999=cmxcix 1000=m \n" +
                        "1001=mi 1002=mii 1003=miii 1004=miv 1005=mv 1006=mvi 1007=mvii 1008=mviii 1009=mix 1010=mx \n" +
                        "1011=mxi 1012=mxii 1013=mxiii 1014=mxiv 1015=mxv 1016=mxvi 1017=mxvii 1018=mxviii 1019=mxix 1020=mxx \n" +
                        "1021=mxxi 1022=mxxii 1023=mxxiii 1024=mxxiv 1025=mxxv 1026=mxxvi 1027=mxxvii 1028=mxxviii 1029=mxxix 1030=mxxx \n" +
                        "1031=mxxxi 1032=mxxxii 1033=mxxxiii 1034=mxxxiv 1035=mxxxv 1036=mxxxvi 1037=mxxxvii 1038=mxxxviii 1039=mxxxix 1040=mxl \n" +
                        "1041=mxli 1042=mxlii 1043=mxliii 1044=mxliv 1045=mxlv 1046=mxlvi 1047=mxlvii 1048=mxlviii 1049=mxlix 1050=ml \n" +
                        "1051=mli 1052=mlii 1053=mliii 1054=mliv 1055=mlv 1056=mlvi 1057=mlvii 1058=mlviii 1059=mlix 1060=mlx \n" +
                        "1061=mlxi 1062=mlxii 1063=mlxiii 1064=mlxiv 1065=mlxv 1066=mlxvi 1067=mlxvii 1068=mlxviii 1069=mlxix 1070=mlxx \n" +
                        "1071=mlxxi 1072=mlxxii 1073=mlxxiii 1074=mlxxiv 1075=mlxxv 1076=mlxxvi 1077=mlxxvii 1078=mlxxviii 1079=mlxxix 1080=mlxxx \n" +
                        "1081=mlxxxi 1082=mlxxxii 1083=mlxxxiii 1084=mlxxxiv 1085=mlxxxv 1086=mlxxxvi 1087=mlxxxvii 1088=mlxxxviii 1089=mlxxxix 1090=mxc \n" +
                        "1091=mxci 1092=mxcii 1093=mxciii 1094=mxciv 1095=mxcv 1096=mxcvi 1097=mxcvii 1098=mxcviii 1099=mxcix 1100=mc \n" +
                        "1101=mci 1102=mcii 1103=mciii 1104=mciv 1105=mcv 1106=mcvi 1107=mcvii 1108=mcviii 1109=mcix 1110=mcx \n" +
                        "1111=mcxi 1112=mcxii 1113=mcxiii 1114=mcxiv 1115=mcxv 1116=mcxvi 1117=mcxvii 1118=mcxviii 1119=mcxix 1120=mcxx \n" +
                        "1121=mcxxi 1122=mcxxii 1123=mcxxiii 1124=mcxxiv 1125=mcxxv 1126=mcxxvi 1127=mcxxvii 1128=mcxxviii 1129=mcxxix 1130=mcxxx \n" +
                        "1131=mcxxxi 1132=mcxxxii 1133=mcxxxiii 1134=mcxxxiv 1135=mcxxxv 1136=mcxxxvi 1137=mcxxxvii 1138=mcxxxviii 1139=mcxxxix 1140=mcxl \n" +
                        "1141=mcxli 1142=mcxlii 1143=mcxliii 1144=mcxliv 1145=mcxlv 1146=mcxlvi 1147=mcxlvii 1148=mcxlviii 1149=mcxlix 1150=mcl \n" +
                        "1151=mcli 1152=mclii 1153=mcliii 1154=mcliv 1155=mclv 1156=mclvi 1157=mclvii 1158=mclviii 1159=mclix 1160=mclx \n" +
                        "1161=mclxi 1162=mclxii 1163=mclxiii 1164=mclxiv 1165=mclxv 1166=mclxvi 1167=mclxvii 1168=mclxviii 1169=mclxix 1170=mclxx \n" +
                        "1171=mclxxi 1172=mclxxii 1173=mclxxiii 1174=mclxxiv 1175=mclxxv 1176=mclxxvi 1177=mclxxvii 1178=mclxxviii 1179=mclxxix 1180=mclxxx \n" +
                        "1181=mclxxxi 1182=mclxxxii 1183=mclxxxiii 1184=mclxxxiv 1185=mclxxxv 1186=mclxxxvi 1187=mclxxxvii 1188=mclxxxviii 1189=mclxxxix 1190=mcxc \n" +
                        "1191=mcxci 1192=mcxcii 1193=mcxciii 1194=mcxciv 1195=mcxcv 1196=mcxcvi 1197=mcxcvii 1198=mcxcviii 1199=mcxcix 1200=mcc \n" +
                        "1201=mcci 1202=mccii 1203=mcciii 1204=mcciv 1205=mccv 1206=mccvi 1207=mccvii 1208=mccviii 1209=mccix 1210=mccx \n" +
                        "1211=mccxi 1212=mccxii 1213=mccxiii 1214=mccxiv 1215=mccxv 1216=mccxvi 1217=mccxvii 1218=mccxviii 1219=mccxix 1220=mccxx \n" +
                        "1221=mccxxi 1222=mccxxii 1223=mccxxiii 1224=mccxxiv 1225=mccxxv 1226=mccxxvi 1227=mccxxvii 1228=mccxxviii 1229=mccxxix 1230=mccxxx \n" +
                        "1231=mccxxxi 1232=mccxxxii 1233=mccxxxiii 1234=mccxxxiv 1235=mccxxxv 1236=mccxxxvi 1237=mccxxxvii 1238=mccxxxviii 1239=mccxxxix 1240=mccxl \n" +
                        "1241=mccxli 1242=mccxlii 1243=mccxliii 1244=mccxliv 1245=mccxlv 1246=mccxlvi 1247=mccxlvii 1248=mccxlviii 1249=mccxlix 1250=mccl \n" +
                        "1251=mccli 1252=mcclii 1253=mccliii 1254=mccliv 1255=mcclv 1256=mcclvi 1257=mcclvii 1258=mcclviii 1259=mcclix 1260=mcclx \n" +
                        "1261=mcclxi 1262=mcclxii 1263=mcclxiii 1264=mcclxiv 1265=mcclxv 1266=mcclxvi 1267=mcclxvii 1268=mcclxviii 1269=mcclxix 1270=mcclxx \n" +
                        "1271=mcclxxi 1272=mcclxxii 1273=mcclxxiii 1274=mcclxxiv 1275=mcclxxv 1276=mcclxxvi 1277=mcclxxvii 1278=mcclxxviii 1279=mcclxxix 1280=mcclxxx \n" +
                        "1281=mcclxxxi 1282=mcclxxxii 1283=mcclxxxiii 1284=mcclxxxiv 1285=mcclxxxv 1286=mcclxxxvi 1287=mcclxxxvii 1288=mcclxxxviii 1289=mcclxxxix 1290=mccxc \n" +
                        "1291=mccxci 1292=mccxcii 1293=mccxciii 1294=mccxciv 1295=mccxcv 1296=mccxcvi 1297=mccxcvii 1298=mccxcviii 1299=mccxcix 1300=mccc \n" +
                        "1301=mccci 1302=mcccii 1303=mccciii 1304=mccciv 1305=mcccv 1306=mcccvi 1307=mcccvii 1308=mcccviii 1309=mcccix 1310=mcccx \n" +
                        "1311=mcccxi 1312=mcccxii 1313=mcccxiii 1314=mcccxiv 1315=mcccxv 1316=mcccxvi 1317=mcccxvii 1318=mcccxviii 1319=mcccxix 1320=mcccxx \n" +
                        "1321=mcccxxi 1322=mcccxxii 1323=mcccxxiii 1324=mcccxxiv 1325=mcccxxv 1326=mcccxxvi 1327=mcccxxvii 1328=mcccxxviii 1329=mcccxxix 1330=mcccxxx \n" +
                        "1331=mcccxxxi 1332=mcccxxxii 1333=mcccxxxiii 1334=mcccxxxiv 1335=mcccxxxv 1336=mcccxxxvi 1337=mcccxxxvii 1338=mcccxxxviii 1339=mcccxxxix 1340=mcccxl \n" +
                        "1341=mcccxli 1342=mcccxlii 1343=mcccxliii 1344=mcccxliv 1345=mcccxlv 1346=mcccxlvi 1347=mcccxlvii 1348=mcccxlviii 1349=mcccxlix 1350=mcccl \n" +
                        "1351=mcccli 1352=mccclii 1353=mcccliii 1354=mcccliv 1355=mccclv 1356=mccclvi 1357=mccclvii 1358=mccclviii 1359=mccclix 1360=mccclx \n" +
                        "1361=mccclxi 1362=mccclxii 1363=mccclxiii 1364=mccclxiv 1365=mccclxv 1366=mccclxvi 1367=mccclxvii 1368=mccclxviii 1369=mccclxix 1370=mccclxx \n" +
                        "1371=mccclxxi 1372=mccclxxii 1373=mccclxxiii 1374=mccclxxiv 1375=mccclxxv 1376=mccclxxvi 1377=mccclxxvii 1378=mccclxxviii 1379=mccclxxix 1380=mccclxxx \n" +
                        "1381=mccclxxxi 1382=mccclxxxii 1383=mccclxxxiii 1384=mccclxxxiv 1385=mccclxxxv 1386=mccclxxxvi 1387=mccclxxxvii 1388=mccclxxxviii 1389=mccclxxxix 1390=mcccxc \n" +
                        "1391=mcccxci 1392=mcccxcii 1393=mcccxciii 1394=mcccxciv 1395=mcccxcv 1396=mcccxcvi 1397=mcccxcvii 1398=mcccxcviii 1399=mcccxcix 1400=mcd \n" +
                        "1401=mcdi 1402=mcdii 1403=mcdiii 1404=mcdiv 1405=mcdv 1406=mcdvi 1407=mcdvii 1408=mcdviii 1409=mcdix 1410=mcdx \n" +
                        "1411=mcdxi 1412=mcdxii 1413=mcdxiii 1414=mcdxiv 1415=mcdxv 1416=mcdxvi 1417=mcdxvii 1418=mcdxviii 1419=mcdxix 1420=mcdxx \n" +
                        "1421=mcdxxi 1422=mcdxxii 1423=mcdxxiii 1424=mcdxxiv 1425=mcdxxv 1426=mcdxxvi 1427=mcdxxvii 1428=mcdxxviii 1429=mcdxxix 1430=mcdxxx \n" +
                        "1431=mcdxxxi 1432=mcdxxxii 1433=mcdxxxiii 1434=mcdxxxiv 1435=mcdxxxv 1436=mcdxxxvi 1437=mcdxxxvii 1438=mcdxxxviii 1439=mcdxxxix 1440=mcdxl \n" +
                        "1441=mcdxli 1442=mcdxlii 1443=mcdxliii 1444=mcdxliv 1445=mcdxlv 1446=mcdxlvi 1447=mcdxlvii 1448=mcdxlviii 1449=mcdxlix 1450=mcdl \n" +
                        "1451=mcdli 1452=mcdlii 1453=mcdliii 1454=mcdliv 1455=mcdlv 1456=mcdlvi 1457=mcdlvii 1458=mcdlviii 1459=mcdlix 1460=mcdlx \n" +
                        "1461=mcdlxi 1462=mcdlxii 1463=mcdlxiii 1464=mcdlxiv 1465=mcdlxv 1466=mcdlxvi 1467=mcdlxvii 1468=mcdlxviii 1469=mcdlxix 1470=mcdlxx \n" +
                        "1471=mcdlxxi 1472=mcdlxxii 1473=mcdlxxiii 1474=mcdlxxiv 1475=mcdlxxv 1476=mcdlxxvi 1477=mcdlxxvii 1478=mcdlxxviii 1479=mcdlxxix 1480=mcdlxxx \n" +
                        "1481=mcdlxxxi 1482=mcdlxxxii 1483=mcdlxxxiii 1484=mcdlxxxiv 1485=mcdlxxxv 1486=mcdlxxxvi 1487=mcdlxxxvii 1488=mcdlxxxviii 1489=mcdlxxxix 1490=mcdxc \n" +
                        "1491=mcdxci 1492=mcdxcii 1493=mcdxciii 1494=mcdxciv 1495=mcdxcv 1496=mcdxcvi 1497=mcdxcvii 1498=mcdxcviii 1499=mcdxcix 1500=md \n" +
                        "1501=mdi 1502=mdii 1503=mdiii 1504=mdiv 1505=mdv 1506=mdvi 1507=mdvii 1508=mdviii 1509=mdix 1510=mdx \n" +
                        "1511=mdxi 1512=mdxii 1513=mdxiii 1514=mdxiv 1515=mdxv 1516=mdxvi 1517=mdxvii 1518=mdxviii 1519=mdxix 1520=mdxx \n" +
                        "1521=mdxxi 1522=mdxxii 1523=mdxxiii 1524=mdxxiv 1525=mdxxv 1526=mdxxvi 1527=mdxxvii 1528=mdxxviii 1529=mdxxix 1530=mdxxx \n" +
                        "1531=mdxxxi 1532=mdxxxii 1533=mdxxxiii 1534=mdxxxiv 1535=mdxxxv 1536=mdxxxvi 1537=mdxxxvii 1538=mdxxxviii 1539=mdxxxix 1540=mdxl \n" +
                        "1541=mdxli 1542=mdxlii 1543=mdxliii 1544=mdxliv 1545=mdxlv 1546=mdxlvi 1547=mdxlvii 1548=mdxlviii 1549=mdxlix 1550=mdl \n" +
                        "1551=mdli 1552=mdlii 1553=mdliii 1554=mdliv 1555=mdlv 1556=mdlvi 1557=mdlvii 1558=mdlviii 1559=mdlix 1560=mdlx \n" +
                        "1561=mdlxi 1562=mdlxii 1563=mdlxiii 1564=mdlxiv 1565=mdlxv 1566=mdlxvi 1567=mdlxvii 1568=mdlxviii 1569=mdlxix 1570=mdlxx \n" +
                        "1571=mdlxxi 1572=mdlxxii 1573=mdlxxiii 1574=mdlxxiv 1575=mdlxxv 1576=mdlxxvi 1577=mdlxxvii 1578=mdlxxviii 1579=mdlxxix 1580=mdlxxx \n" +
                        "1581=mdlxxxi 1582=mdlxxxii 1583=mdlxxxiii 1584=mdlxxxiv 1585=mdlxxxv 1586=mdlxxxvi 1587=mdlxxxvii 1588=mdlxxxviii 1589=mdlxxxix 1590=mdxc \n" +
                        "1591=mdxci 1592=mdxcii 1593=mdxciii 1594=mdxciv 1595=mdxcv 1596=mdxcvi 1597=mdxcvii 1598=mdxcviii 1599=mdxcix 1600=mdc \n" +
                        "1601=mdci 1602=mdcii 1603=mdciii 1604=mdciv 1605=mdcv 1606=mdcvi 1607=mdcvii 1608=mdcviii 1609=mdcix 1610=mdcx \n" +
                        "1611=mdcxi 1612=mdcxii 1613=mdcxiii 1614=mdcxiv 1615=mdcxv 1616=mdcxvi 1617=mdcxvii 1618=mdcxviii 1619=mdcxix 1620=mdcxx \n" +
                        "1621=mdcxxi 1622=mdcxxii 1623=mdcxxiii 1624=mdcxxiv 1625=mdcxxv 1626=mdcxxvi 1627=mdcxxvii 1628=mdcxxviii 1629=mdcxxix 1630=mdcxxx \n" +
                        "1631=mdcxxxi 1632=mdcxxxii 1633=mdcxxxiii 1634=mdcxxxiv 1635=mdcxxxv 1636=mdcxxxvi 1637=mdcxxxvii 1638=mdcxxxviii 1639=mdcxxxix 1640=mdcxl \n" +
                        "1641=mdcxli 1642=mdcxlii 1643=mdcxliii 1644=mdcxliv 1645=mdcxlv 1646=mdcxlvi 1647=mdcxlvii 1648=mdcxlviii 1649=mdcxlix 1650=mdcl \n" +
                        "1651=mdcli 1652=mdclii 1653=mdcliii 1654=mdcliv 1655=mdclv 1656=mdclvi 1657=mdclvii 1658=mdclviii 1659=mdclix 1660=mdclx \n" +
                        "1661=mdclxi 1662=mdclxii 1663=mdclxiii 1664=mdclxiv 1665=mdclxv 1666=mdclxvi 1667=mdclxvii 1668=mdclxviii 1669=mdclxix 1670=mdclxx \n" +
                        "1671=mdclxxi 1672=mdclxxii 1673=mdclxxiii 1674=mdclxxiv 1675=mdclxxv 1676=mdclxxvi 1677=mdclxxvii 1678=mdclxxviii 1679=mdclxxix 1680=mdclxxx \n" +
                        "1681=mdclxxxi 1682=mdclxxxii 1683=mdclxxxiii 1684=mdclxxxiv 1685=mdclxxxv 1686=mdclxxxvi 1687=mdclxxxvii 1688=mdclxxxviii 1689=mdclxxxix 1690=mdcxc \n" +
                        "1691=mdcxci 1692=mdcxcii 1693=mdcxciii 1694=mdcxciv 1695=mdcxcv 1696=mdcxcvi 1697=mdcxcvii 1698=mdcxcviii 1699=mdcxcix 1700=mdcc \n" +
                        "1701=mdcci 1702=mdccii 1703=mdcciii 1704=mdcciv 1705=mdccv 1706=mdccvi 1707=mdccvii 1708=mdccviii 1709=mdccix 1710=mdccx \n" +
                        "1711=mdccxi 1712=mdccxii 1713=mdccxiii 1714=mdccxiv 1715=mdccxv 1716=mdccxvi 1717=mdccxvii 1718=mdccxviii 1719=mdccxix 1720=mdccxx \n" +
                        "1721=mdccxxi 1722=mdccxxii 1723=mdccxxiii 1724=mdccxxiv 1725=mdccxxv 1726=mdccxxvi 1727=mdccxxvii 1728=mdccxxviii 1729=mdccxxix 1730=mdccxxx \n" +
                        "1731=mdccxxxi 1732=mdccxxxii 1733=mdccxxxiii 1734=mdccxxxiv 1735=mdccxxxv 1736=mdccxxxvi 1737=mdccxxxvii 1738=mdccxxxviii 1739=mdccxxxix 1740=mdccxl \n" +
                        "1741=mdccxli 1742=mdccxlii 1743=mdccxliii 1744=mdccxliv 1745=mdccxlv 1746=mdccxlvi 1747=mdccxlvii 1748=mdccxlviii 1749=mdccxlix 1750=mdccl \n" +
                        "1751=mdccli 1752=mdcclii 1753=mdccliii 1754=mdccliv 1755=mdcclv 1756=mdcclvi 1757=mdcclvii 1758=mdcclviii 1759=mdcclix 1760=mdcclx \n" +
                        "1761=mdcclxi 1762=mdcclxii 1763=mdcclxiii 1764=mdcclxiv 1765=mdcclxv 1766=mdcclxvi 1767=mdcclxvii 1768=mdcclxviii 1769=mdcclxix 1770=mdcclxx \n" +
                        "1771=mdcclxxi 1772=mdcclxxii 1773=mdcclxxiii 1774=mdcclxxiv 1775=mdcclxxv 1776=mdcclxxvi 1777=mdcclxxvii 1778=mdcclxxviii 1779=mdcclxxix 1780=mdcclxxx \n" +
                        "1781=mdcclxxxi 1782=mdcclxxxii 1783=mdcclxxxiii 1784=mdcclxxxiv 1785=mdcclxxxv 1786=mdcclxxxvi 1787=mdcclxxxvii 1788=mdcclxxxviii 1789=mdcclxxxix 1790=mdccxc \n" +
                        "1791=mdccxci 1792=mdccxcii 1793=mdccxciii 1794=mdccxciv 1795=mdccxcv 1796=mdccxcvi 1797=mdccxcvii 1798=mdccxcviii 1799=mdccxcix 1800=mdccc \n" +
                        "1801=mdccci 1802=mdcccii 1803=mdccciii 1804=mdccciv 1805=mdcccv 1806=mdcccvi 1807=mdcccvii 1808=mdcccviii 1809=mdcccix 1810=mdcccx \n" +
                        "1811=mdcccxi 1812=mdcccxii 1813=mdcccxiii 1814=mdcccxiv 1815=mdcccxv 1816=mdcccxvi 1817=mdcccxvii 1818=mdcccxviii 1819=mdcccxix 1820=mdcccxx \n" +
                        "1821=mdcccxxi 1822=mdcccxxii 1823=mdcccxxiii 1824=mdcccxxiv 1825=mdcccxxv 1826=mdcccxxvi 1827=mdcccxxvii 1828=mdcccxxviii 1829=mdcccxxix 1830=mdcccxxx \n" +
                        "1831=mdcccxxxi 1832=mdcccxxxii 1833=mdcccxxxiii 1834=mdcccxxxiv 1835=mdcccxxxv 1836=mdcccxxxvi 1837=mdcccxxxvii 1838=mdcccxxxviii 1839=mdcccxxxix 1840=mdcccxl \n" +
                        "1841=mdcccxli 1842=mdcccxlii 1843=mdcccxliii 1844=mdcccxliv 1845=mdcccxlv 1846=mdcccxlvi 1847=mdcccxlvii 1848=mdcccxlviii 1849=mdcccxlix 1850=mdcccl \n" +
                        "1851=mdcccli 1852=mdccclii 1853=mdcccliii 1854=mdcccliv 1855=mdccclv 1856=mdccclvi 1857=mdccclvii 1858=mdccclviii 1859=mdccclix 1860=mdccclx \n" +
                        "1861=mdccclxi 1862=mdccclxii 1863=mdccclxiii 1864=mdccclxiv 1865=mdccclxv 1866=mdccclxvi 1867=mdccclxvii 1868=mdccclxviii 1869=mdccclxix 1870=mdccclxx \n" +
                        "1871=mdccclxxi 1872=mdccclxxii 1873=mdccclxxiii 1874=mdccclxxiv 1875=mdccclxxv 1876=mdccclxxvi 1877=mdccclxxvii 1878=mdccclxxviii 1879=mdccclxxix 1880=mdccclxxx \n" +
                        "1881=mdccclxxxi 1882=mdccclxxxii 1883=mdccclxxxiii 1884=mdccclxxxiv 1885=mdccclxxxv 1886=mdccclxxxvi 1887=mdccclxxxvii 1888=mdccclxxxviii 1889=mdccclxxxix 1890=mdcccxc \n" +
                        "1891=mdcccxci 1892=mdcccxcii 1893=mdcccxciii 1894=mdcccxciv 1895=mdcccxcv 1896=mdcccxcvi 1897=mdcccxcvii 1898=mdcccxcviii 1899=mdcccxcix 1900=mcm \n" +
                        "1901=mcmi 1902=mcmii 1903=mcmiii 1904=mcmiv 1905=mcmv 1906=mcmvi 1907=mcmvii 1908=mcmviii 1909=mcmix 1910=mcmx \n" +
                        "1911=mcmxi 1912=mcmxii 1913=mcmxiii 1914=mcmxiv 1915=mcmxv 1916=mcmxvi 1917=mcmxvii 1918=mcmxviii 1919=mcmxix 1920=mcmxx \n" +
                        "1921=mcmxxi 1922=mcmxxii 1923=mcmxxiii 1924=mcmxxiv 1925=mcmxxv 1926=mcmxxvi 1927=mcmxxvii 1928=mcmxxviii 1929=mcmxxix 1930=mcmxxx \n" +
                        "1931=mcmxxxi 1932=mcmxxxii 1933=mcmxxxiii 1934=mcmxxxiv 1935=mcmxxxv 1936=mcmxxxvi 1937=mcmxxxvii 1938=mcmxxxviii 1939=mcmxxxix 1940=mcmxl \n" +
                        "1941=mcmxli 1942=mcmxlii 1943=mcmxliii 1944=mcmxliv 1945=mcmxlv 1946=mcmxlvi 1947=mcmxlvii 1948=mcmxlviii 1949=mcmxlix 1950=mcml \n" +
                        "1951=mcmli 1952=mcmlii 1953=mcmliii 1954=mcmliv 1955=mcmlv 1956=mcmlvi 1957=mcmlvii 1958=mcmlviii 1959=mcmlix 1960=mcmlx \n" +
                        "1961=mcmlxi 1962=mcmlxii 1963=mcmlxiii 1964=mcmlxiv 1965=mcmlxv 1966=mcmlxvi 1967=mcmlxvii 1968=mcmlxviii 1969=mcmlxix 1970=mcmlxx \n" +
                        "1971=mcmlxxi 1972=mcmlxxii 1973=mcmlxxiii 1974=mcmlxxiv 1975=mcmlxxv 1976=mcmlxxvi 1977=mcmlxxvii 1978=mcmlxxviii 1979=mcmlxxix 1980=mcmlxxx \n" +
                        "1981=mcmlxxxi 1982=mcmlxxxii 1983=mcmlxxxiii 1984=mcmlxxxiv 1985=mcmlxxxv 1986=mcmlxxxvi 1987=mcmlxxxvii 1988=mcmlxxxviii 1989=mcmlxxxix 1990=mcmxc \n" +
                        "1991=mcmxci 1992=mcmxcii 1993=mcmxciii 1994=mcmxciv 1995=mcmxcv 1996=mcmxcvi 1997=mcmxcvii 1998=mcmxcviii 1999=mcmxcix 2000=mm \n" +
                        "2001=mmi 2002=mmii 2003=mmiii 2004=mmiv 2005=mmv 2006=mmvi 2007=mmvii 2008=mmviii 2009=mmix 2010=mmx \n" +
                        "2011=mmxi 2012=mmxii 2013=mmxiii 2014=mmxiv 2015=mmxv 2016=mmxvi 2017=mmxvii 2018=mmxviii 2019=mmxix 2020=mmxx \n" +
                        "2021=mmxxi 2022=mmxxii 2023=mmxxiii 2024=mmxxiv 2025=mmxxv 2026=mmxxvi 2027=mmxxvii 2028=mmxxviii 2029=mmxxix 2030=mmxxx \n" +
                        "2031=mmxxxi 2032=mmxxxii 2033=mmxxxiii 2034=mmxxxiv 2035=mmxxxv 2036=mmxxxvi 2037=mmxxxvii 2038=mmxxxviii 2039=mmxxxix 2040=mmxl \n" +
                        "2041=mmxli 2042=mmxlii 2043=mmxliii 2044=mmxliv 2045=mmxlv 2046=mmxlvi 2047=mmxlvii 2048=mmxlviii 2049=mmxlix 2050=mml \n" +
                        "2051=mmli 2052=mmlii 2053=mmliii 2054=mmliv 2055=mmlv 2056=mmlvi 2057=mmlvii 2058=mmlviii 2059=mmlix 2060=mmlx \n" +
                        "2061=mmlxi 2062=mmlxii 2063=mmlxiii 2064=mmlxiv 2065=mmlxv 2066=mmlxvi 2067=mmlxvii 2068=mmlxviii 2069=mmlxix 2070=mmlxx \n" +
                        "2071=mmlxxi 2072=mmlxxii 2073=mmlxxiii 2074=mmlxxiv 2075=mmlxxv 2076=mmlxxvi 2077=mmlxxvii 2078=mmlxxviii 2079=mmlxxix 2080=mmlxxx \n" +
                        "2081=mmlxxxi 2082=mmlxxxii 2083=mmlxxxiii 2084=mmlxxxiv 2085=mmlxxxv 2086=mmlxxxvi 2087=mmlxxxvii 2088=mmlxxxviii 2089=mmlxxxix 2090=mmxc \n" +
                        "2091=mmxci 2092=mmxcii 2093=mmxciii 2094=mmxciv 2095=mmxcv 2096=mmxcvi 2097=mmxcvii 2098=mmxcviii 2099=mmxcix 2100=mmc \n" +
                        "2101=mmci 2102=mmcii 2103=mmciii 2104=mmciv 2105=mmcv 2106=mmcvi 2107=mmcvii 2108=mmcviii 2109=mmcix 2110=mmcx \n" +
                        "2111=mmcxi 2112=mmcxii 2113=mmcxiii 2114=mmcxiv 2115=mmcxv 2116=mmcxvi 2117=mmcxvii 2118=mmcxviii 2119=mmcxix 2120=mmcxx \n" +
                        "2121=mmcxxi 2122=mmcxxii 2123=mmcxxiii 2124=mmcxxiv 2125=mmcxxv 2126=mmcxxvi 2127=mmcxxvii 2128=mmcxxviii 2129=mmcxxix 2130=mmcxxx \n" +
                        "2131=mmcxxxi 2132=mmcxxxii 2133=mmcxxxiii 2134=mmcxxxiv 2135=mmcxxxv 2136=mmcxxxvi 2137=mmcxxxvii 2138=mmcxxxviii 2139=mmcxxxix 2140=mmcxl \n" +
                        "2141=mmcxli 2142=mmcxlii 2143=mmcxliii 2144=mmcxliv 2145=mmcxlv 2146=mmcxlvi 2147=mmcxlvii 2148=mmcxlviii 2149=mmcxlix 2150=mmcl \n" +
                        "2151=mmcli 2152=mmclii 2153=mmcliii 2154=mmcliv 2155=mmclv 2156=mmclvi 2157=mmclvii 2158=mmclviii 2159=mmclix 2160=mmclx \n" +
                        "2161=mmclxi 2162=mmclxii 2163=mmclxiii 2164=mmclxiv 2165=mmclxv 2166=mmclxvi 2167=mmclxvii 2168=mmclxviii 2169=mmclxix 2170=mmclxx \n" +
                        "2171=mmclxxi 2172=mmclxxii 2173=mmclxxiii 2174=mmclxxiv 2175=mmclxxv 2176=mmclxxvi 2177=mmclxxvii 2178=mmclxxviii 2179=mmclxxix 2180=mmclxxx \n" +
                        "2181=mmclxxxi 2182=mmclxxxii 2183=mmclxxxiii 2184=mmclxxxiv 2185=mmclxxxv 2186=mmclxxxvi 2187=mmclxxxvii 2188=mmclxxxviii 2189=mmclxxxix 2190=mmcxc \n" +
                        "2191=mmcxci 2192=mmcxcii 2193=mmcxciii 2194=mmcxciv 2195=mmcxcv 2196=mmcxcvi 2197=mmcxcvii 2198=mmcxcviii 2199=mmcxcix 2200=mmcc \n" +
                        "2201=mmcci 2202=mmccii 2203=mmcciii 2204=mmcciv 2205=mmccv 2206=mmccvi 2207=mmccvii 2208=mmccviii 2209=mmccix 2210=mmccx \n" +
                        "2211=mmccxi 2212=mmccxii 2213=mmccxiii 2214=mmccxiv 2215=mmccxv 2216=mmccxvi 2217=mmccxvii 2218=mmccxviii 2219=mmccxix 2220=mmccxx \n" +
                        "2221=mmccxxi 2222=mmccxxii 2223=mmccxxiii 2224=mmccxxiv 2225=mmccxxv 2226=mmccxxvi 2227=mmccxxvii 2228=mmccxxviii 2229=mmccxxix 2230=mmccxxx \n" +
                        "2231=mmccxxxi 2232=mmccxxxii 2233=mmccxxxiii 2234=mmccxxxiv 2235=mmccxxxv 2236=mmccxxxvi 2237=mmccxxxvii 2238=mmccxxxviii 2239=mmccxxxix 2240=mmccxl \n" +
                        "2241=mmccxli 2242=mmccxlii 2243=mmccxliii 2244=mmccxliv 2245=mmccxlv 2246=mmccxlvi 2247=mmccxlvii 2248=mmccxlviii 2249=mmccxlix 2250=mmccl \n" +
                        "2251=mmccli 2252=mmcclii 2253=mmccliii 2254=mmccliv 2255=mmcclv 2256=mmcclvi 2257=mmcclvii 2258=mmcclviii 2259=mmcclix 2260=mmcclx \n" +
                        "2261=mmcclxi 2262=mmcclxii 2263=mmcclxiii 2264=mmcclxiv 2265=mmcclxv 2266=mmcclxvi 2267=mmcclxvii 2268=mmcclxviii 2269=mmcclxix 2270=mmcclxx \n" +
                        "2271=mmcclxxi 2272=mmcclxxii 2273=mmcclxxiii 2274=mmcclxxiv 2275=mmcclxxv 2276=mmcclxxvi 2277=mmcclxxvii 2278=mmcclxxviii 2279=mmcclxxix 2280=mmcclxxx \n" +
                        "2281=mmcclxxxi 2282=mmcclxxxii 2283=mmcclxxxiii 2284=mmcclxxxiv 2285=mmcclxxxv 2286=mmcclxxxvi 2287=mmcclxxxvii 2288=mmcclxxxviii 2289=mmcclxxxix 2290=mmccxc \n" +
                        "2291=mmccxci 2292=mmccxcii 2293=mmccxciii 2294=mmccxciv 2295=mmccxcv 2296=mmccxcvi 2297=mmccxcvii 2298=mmccxcviii 2299=mmccxcix 2300=mmccc \n" +
                        "2301=mmccci 2302=mmcccii 2303=mmccciii 2304=mmccciv 2305=mmcccv 2306=mmcccvi 2307=mmcccvii 2308=mmcccviii 2309=mmcccix 2310=mmcccx \n" +
                        "2311=mmcccxi 2312=mmcccxii 2313=mmcccxiii 2314=mmcccxiv 2315=mmcccxv 2316=mmcccxvi 2317=mmcccxvii 2318=mmcccxviii 2319=mmcccxix 2320=mmcccxx \n" +
                        "2321=mmcccxxi 2322=mmcccxxii 2323=mmcccxxiii 2324=mmcccxxiv 2325=mmcccxxv 2326=mmcccxxvi 2327=mmcccxxvii 2328=mmcccxxviii 2329=mmcccxxix 2330=mmcccxxx \n" +
                        "2331=mmcccxxxi 2332=mmcccxxxii 2333=mmcccxxxiii 2334=mmcccxxxiv 2335=mmcccxxxv 2336=mmcccxxxvi 2337=mmcccxxxvii 2338=mmcccxxxviii 2339=mmcccxxxix 2340=mmcccxl \n" +
                        "2341=mmcccxli 2342=mmcccxlii 2343=mmcccxliii 2344=mmcccxliv 2345=mmcccxlv 2346=mmcccxlvi 2347=mmcccxlvii 2348=mmcccxlviii 2349=mmcccxlix 2350=mmcccl \n" +
                        "2351=mmcccli 2352=mmccclii 2353=mmcccliii 2354=mmcccliv 2355=mmccclv 2356=mmccclvi 2357=mmccclvii 2358=mmccclviii 2359=mmccclix 2360=mmccclx \n" +
                        "2361=mmccclxi 2362=mmccclxii 2363=mmccclxiii 2364=mmccclxiv 2365=mmccclxv 2366=mmccclxvi 2367=mmccclxvii 2368=mmccclxviii 2369=mmccclxix 2370=mmccclxx \n" +
                        "2371=mmccclxxi 2372=mmccclxxii 2373=mmccclxxiii 2374=mmccclxxiv 2375=mmccclxxv 2376=mmccclxxvi 2377=mmccclxxvii 2378=mmccclxxviii 2379=mmccclxxix 2380=mmccclxxx \n" +
                        "2381=mmccclxxxi 2382=mmccclxxxii 2383=mmccclxxxiii 2384=mmccclxxxiv 2385=mmccclxxxv 2386=mmccclxxxvi 2387=mmccclxxxvii 2388=mmccclxxxviii 2389=mmccclxxxix 2390=mmcccxc \n" +
                        "2391=mmcccxci 2392=mmcccxcii 2393=mmcccxciii 2394=mmcccxciv 2395=mmcccxcv 2396=mmcccxcvi 2397=mmcccxcvii 2398=mmcccxcviii 2399=mmcccxcix 2400=mmcd \n" +
                        "2401=mmcdi 2402=mmcdii 2403=mmcdiii 2404=mmcdiv 2405=mmcdv 2406=mmcdvi 2407=mmcdvii 2408=mmcdviii 2409=mmcdix 2410=mmcdx \n" +
                        "2411=mmcdxi 2412=mmcdxii 2413=mmcdxiii 2414=mmcdxiv 2415=mmcdxv 2416=mmcdxvi 2417=mmcdxvii 2418=mmcdxviii 2419=mmcdxix 2420=mmcdxx \n" +
                        "2421=mmcdxxi 2422=mmcdxxii 2423=mmcdxxiii 2424=mmcdxxiv 2425=mmcdxxv 2426=mmcdxxvi 2427=mmcdxxvii 2428=mmcdxxviii 2429=mmcdxxix 2430=mmcdxxx \n" +
                        "2431=mmcdxxxi 2432=mmcdxxxii 2433=mmcdxxxiii 2434=mmcdxxxiv 2435=mmcdxxxv 2436=mmcdxxxvi 2437=mmcdxxxvii 2438=mmcdxxxviii 2439=mmcdxxxix 2440=mmcdxl \n" +
                        "2441=mmcdxli 2442=mmcdxlii 2443=mmcdxliii 2444=mmcdxliv 2445=mmcdxlv 2446=mmcdxlvi 2447=mmcdxlvii 2448=mmcdxlviii 2449=mmcdxlix 2450=mmcdl \n" +
                        "2451=mmcdli 2452=mmcdlii 2453=mmcdliii 2454=mmcdliv 2455=mmcdlv 2456=mmcdlvi 2457=mmcdlvii 2458=mmcdlviii 2459=mmcdlix 2460=mmcdlx \n" +
                        "2461=mmcdlxi 2462=mmcdlxii 2463=mmcdlxiii 2464=mmcdlxiv 2465=mmcdlxv 2466=mmcdlxvi 2467=mmcdlxvii 2468=mmcdlxviii 2469=mmcdlxix 2470=mmcdlxx \n" +
                        "2471=mmcdlxxi 2472=mmcdlxxii 2473=mmcdlxxiii 2474=mmcdlxxiv 2475=mmcdlxxv 2476=mmcdlxxvi 2477=mmcdlxxvii 2478=mmcdlxxviii 2479=mmcdlxxix 2480=mmcdlxxx \n" +
                        "2481=mmcdlxxxi 2482=mmcdlxxxii 2483=mmcdlxxxiii 2484=mmcdlxxxiv 2485=mmcdlxxxv 2486=mmcdlxxxvi 2487=mmcdlxxxvii 2488=mmcdlxxxviii 2489=mmcdlxxxix 2490=mmcdxc \n" +
                        "2491=mmcdxci 2492=mmcdxcii 2493=mmcdxciii 2494=mmcdxciv 2495=mmcdxcv 2496=mmcdxcvi 2497=mmcdxcvii 2498=mmcdxcviii 2499=mmcdxcix 2500=mmd \n" +
                        "2501=mmdi 2502=mmdii 2503=mmdiii 2504=mmdiv 2505=mmdv 2506=mmdvi 2507=mmdvii 2508=mmdviii 2509=mmdix 2510=mmdx \n" +
                        "2511=mmdxi 2512=mmdxii 2513=mmdxiii 2514=mmdxiv 2515=mmdxv 2516=mmdxvi 2517=mmdxvii 2518=mmdxviii 2519=mmdxix 2520=mmdxx \n" +
                        "2521=mmdxxi 2522=mmdxxii 2523=mmdxxiii 2524=mmdxxiv 2525=mmdxxv 2526=mmdxxvi 2527=mmdxxvii 2528=mmdxxviii 2529=mmdxxix 2530=mmdxxx \n" +
                        "2531=mmdxxxi 2532=mmdxxxii 2533=mmdxxxiii 2534=mmdxxxiv 2535=mmdxxxv 2536=mmdxxxvi 2537=mmdxxxvii 2538=mmdxxxviii 2539=mmdxxxix 2540=mmdxl \n" +
                        "2541=mmdxli 2542=mmdxlii 2543=mmdxliii 2544=mmdxliv 2545=mmdxlv 2546=mmdxlvi 2547=mmdxlvii 2548=mmdxlviii 2549=mmdxlix 2550=mmdl \n" +
                        "2551=mmdli 2552=mmdlii 2553=mmdliii 2554=mmdliv 2555=mmdlv 2556=mmdlvi 2557=mmdlvii 2558=mmdlviii 2559=mmdlix 2560=mmdlx \n" +
                        "2561=mmdlxi 2562=mmdlxii 2563=mmdlxiii 2564=mmdlxiv 2565=mmdlxv 2566=mmdlxvi 2567=mmdlxvii 2568=mmdlxviii 2569=mmdlxix 2570=mmdlxx \n" +
                        "2571=mmdlxxi 2572=mmdlxxii 2573=mmdlxxiii 2574=mmdlxxiv 2575=mmdlxxv 2576=mmdlxxvi 2577=mmdlxxvii 2578=mmdlxxviii 2579=mmdlxxix 2580=mmdlxxx \n" +
                        "2581=mmdlxxxi 2582=mmdlxxxii 2583=mmdlxxxiii 2584=mmdlxxxiv 2585=mmdlxxxv 2586=mmdlxxxvi 2587=mmdlxxxvii 2588=mmdlxxxviii 2589=mmdlxxxix 2590=mmdxc \n" +
                        "2591=mmdxci 2592=mmdxcii 2593=mmdxciii 2594=mmdxciv 2595=mmdxcv 2596=mmdxcvi 2597=mmdxcvii 2598=mmdxcviii 2599=mmdxcix 2600=mmdc \n" +
                        "2601=mmdci 2602=mmdcii 2603=mmdciii 2604=mmdciv 2605=mmdcv 2606=mmdcvi 2607=mmdcvii 2608=mmdcviii 2609=mmdcix 2610=mmdcx \n" +
                        "2611=mmdcxi 2612=mmdcxii 2613=mmdcxiii 2614=mmdcxiv 2615=mmdcxv 2616=mmdcxvi 2617=mmdcxvii 2618=mmdcxviii 2619=mmdcxix 2620=mmdcxx \n" +
                        "2621=mmdcxxi 2622=mmdcxxii 2623=mmdcxxiii 2624=mmdcxxiv 2625=mmdcxxv 2626=mmdcxxvi 2627=mmdcxxvii 2628=mmdcxxviii 2629=mmdcxxix 2630=mmdcxxx \n" +
                        "2631=mmdcxxxi 2632=mmdcxxxii 2633=mmdcxxxiii 2634=mmdcxxxiv 2635=mmdcxxxv 2636=mmdcxxxvi 2637=mmdcxxxvii 2638=mmdcxxxviii 2639=mmdcxxxix 2640=mmdcxl \n" +
                        "2641=mmdcxli 2642=mmdcxlii 2643=mmdcxliii 2644=mmdcxliv 2645=mmdcxlv 2646=mmdcxlvi 2647=mmdcxlvii 2648=mmdcxlviii 2649=mmdcxlix 2650=mmdcl \n" +
                        "2651=mmdcli 2652=mmdclii 2653=mmdcliii 2654=mmdcliv 2655=mmdclv 2656=mmdclvi 2657=mmdclvii 2658=mmdclviii 2659=mmdclix 2660=mmdclx \n" +
                        "2661=mmdclxi 2662=mmdclxii 2663=mmdclxiii 2664=mmdclxiv 2665=mmdclxv 2666=mmdclxvi 2667=mmdclxvii 2668=mmdclxviii 2669=mmdclxix 2670=mmdclxx \n" +
                        "2671=mmdclxxi 2672=mmdclxxii 2673=mmdclxxiii 2674=mmdclxxiv 2675=mmdclxxv 2676=mmdclxxvi 2677=mmdclxxvii 2678=mmdclxxviii 2679=mmdclxxix 2680=mmdclxxx \n" +
                        "2681=mmdclxxxi 2682=mmdclxxxii 2683=mmdclxxxiii 2684=mmdclxxxiv 2685=mmdclxxxv 2686=mmdclxxxvi 2687=mmdclxxxvii 2688=mmdclxxxviii 2689=mmdclxxxix 2690=mmdcxc \n" +
                        "2691=mmdcxci 2692=mmdcxcii 2693=mmdcxciii 2694=mmdcxciv 2695=mmdcxcv 2696=mmdcxcvi 2697=mmdcxcvii 2698=mmdcxcviii 2699=mmdcxcix 2700=mmdcc \n" +
                        "2701=mmdcci 2702=mmdccii 2703=mmdcciii 2704=mmdcciv 2705=mmdccv 2706=mmdccvi 2707=mmdccvii 2708=mmdccviii 2709=mmdccix 2710=mmdccx \n" +
                        "2711=mmdccxi 2712=mmdccxii 2713=mmdccxiii 2714=mmdccxiv 2715=mmdccxv 2716=mmdccxvi 2717=mmdccxvii 2718=mmdccxviii 2719=mmdccxix 2720=mmdccxx \n" +
                        "2721=mmdccxxi 2722=mmdccxxii 2723=mmdccxxiii 2724=mmdccxxiv 2725=mmdccxxv 2726=mmdccxxvi 2727=mmdccxxvii 2728=mmdccxxviii 2729=mmdccxxix 2730=mmdccxxx \n" +
                        "2731=mmdccxxxi 2732=mmdccxxxii 2733=mmdccxxxiii 2734=mmdccxxxiv 2735=mmdccxxxv 2736=mmdccxxxvi 2737=mmdccxxxvii 2738=mmdccxxxviii 2739=mmdccxxxix 2740=mmdccxl \n" +
                        "2741=mmdccxli 2742=mmdccxlii 2743=mmdccxliii 2744=mmdccxliv 2745=mmdccxlv 2746=mmdccxlvi 2747=mmdccxlvii 2748=mmdccxlviii 2749=mmdccxlix 2750=mmdccl \n" +
                        "2751=mmdccli 2752=mmdcclii 2753=mmdccliii 2754=mmdccliv 2755=mmdcclv 2756=mmdcclvi 2757=mmdcclvii 2758=mmdcclviii 2759=mmdcclix 2760=mmdcclx \n" +
                        "2761=mmdcclxi 2762=mmdcclxii 2763=mmdcclxiii 2764=mmdcclxiv 2765=mmdcclxv 2766=mmdcclxvi 2767=mmdcclxvii 2768=mmdcclxviii 2769=mmdcclxix 2770=mmdcclxx \n" +
                        "2771=mmdcclxxi 2772=mmdcclxxii 2773=mmdcclxxiii 2774=mmdcclxxiv 2775=mmdcclxxv 2776=mmdcclxxvi 2777=mmdcclxxvii 2778=mmdcclxxviii 2779=mmdcclxxix 2780=mmdcclxxx \n" +
                        "2781=mmdcclxxxi 2782=mmdcclxxxii 2783=mmdcclxxxiii 2784=mmdcclxxxiv 2785=mmdcclxxxv 2786=mmdcclxxxvi 2787=mmdcclxxxvii 2788=mmdcclxxxviii 2789=mmdcclxxxix 2790=mmdccxc \n" +
                        "2791=mmdccxci 2792=mmdccxcii 2793=mmdccxciii 2794=mmdccxciv 2795=mmdccxcv 2796=mmdccxcvi 2797=mmdccxcvii 2798=mmdccxcviii 2799=mmdccxcix 2800=mmdccc \n" +
                        "2801=mmdccci 2802=mmdcccii 2803=mmdccciii 2804=mmdccciv 2805=mmdcccv 2806=mmdcccvi 2807=mmdcccvii 2808=mmdcccviii 2809=mmdcccix 2810=mmdcccx \n" +
                        "2811=mmdcccxi 2812=mmdcccxii 2813=mmdcccxiii 2814=mmdcccxiv 2815=mmdcccxv 2816=mmdcccxvi 2817=mmdcccxvii 2818=mmdcccxviii 2819=mmdcccxix 2820=mmdcccxx \n" +
                        "2821=mmdcccxxi 2822=mmdcccxxii 2823=mmdcccxxiii 2824=mmdcccxxiv 2825=mmdcccxxv 2826=mmdcccxxvi 2827=mmdcccxxvii 2828=mmdcccxxviii 2829=mmdcccxxix 2830=mmdcccxxx \n" +
                        "2831=mmdcccxxxi 2832=mmdcccxxxii 2833=mmdcccxxxiii 2834=mmdcccxxxiv 2835=mmdcccxxxv 2836=mmdcccxxxvi 2837=mmdcccxxxvii 2838=mmdcccxxxviii 2839=mmdcccxxxix 2840=mmdcccxl \n" +
                        "2841=mmdcccxli 2842=mmdcccxlii 2843=mmdcccxliii 2844=mmdcccxliv 2845=mmdcccxlv 2846=mmdcccxlvi 2847=mmdcccxlvii 2848=mmdcccxlviii 2849=mmdcccxlix 2850=mmdcccl \n" +
                        "2851=mmdcccli 2852=mmdccclii 2853=mmdcccliii 2854=mmdcccliv 2855=mmdccclv 2856=mmdccclvi 2857=mmdccclvii 2858=mmdccclviii 2859=mmdccclix 2860=mmdccclx \n" +
                        "2861=mmdccclxi 2862=mmdccclxii 2863=mmdccclxiii 2864=mmdccclxiv 2865=mmdccclxv 2866=mmdccclxvi 2867=mmdccclxvii 2868=mmdccclxviii 2869=mmdccclxix 2870=mmdccclxx \n" +
                        "2871=mmdccclxxi 2872=mmdccclxxii 2873=mmdccclxxiii 2874=mmdccclxxiv 2875=mmdccclxxv 2876=mmdccclxxvi 2877=mmdccclxxvii 2878=mmdccclxxviii 2879=mmdccclxxix 2880=mmdccclxxx \n" +
                        "2881=mmdccclxxxi 2882=mmdccclxxxii 2883=mmdccclxxxiii 2884=mmdccclxxxiv 2885=mmdccclxxxv 2886=mmdccclxxxvi 2887=mmdccclxxxvii 2888=mmdccclxxxviii 2889=mmdccclxxxix 2890=mmdcccxc \n" +
                        "2891=mmdcccxci 2892=mmdcccxcii 2893=mmdcccxciii 2894=mmdcccxciv 2895=mmdcccxcv 2896=mmdcccxcvi 2897=mmdcccxcvii 2898=mmdcccxcviii 2899=mmdcccxcix 2900=mmcm \n" +
                        "2901=mmcmi 2902=mmcmii 2903=mmcmiii 2904=mmcmiv 2905=mmcmv 2906=mmcmvi 2907=mmcmvii 2908=mmcmviii 2909=mmcmix 2910=mmcmx \n" +
                        "2911=mmcmxi 2912=mmcmxii 2913=mmcmxiii 2914=mmcmxiv 2915=mmcmxv 2916=mmcmxvi 2917=mmcmxvii 2918=mmcmxviii 2919=mmcmxix 2920=mmcmxx \n" +
                        "2921=mmcmxxi 2922=mmcmxxii 2923=mmcmxxiii 2924=mmcmxxiv 2925=mmcmxxv 2926=mmcmxxvi 2927=mmcmxxvii 2928=mmcmxxviii 2929=mmcmxxix 2930=mmcmxxx \n" +
                        "2931=mmcmxxxi 2932=mmcmxxxii 2933=mmcmxxxiii 2934=mmcmxxxiv 2935=mmcmxxxv 2936=mmcmxxxvi 2937=mmcmxxxvii 2938=mmcmxxxviii 2939=mmcmxxxix 2940=mmcmxl \n" +
                        "2941=mmcmxli 2942=mmcmxlii 2943=mmcmxliii 2944=mmcmxliv 2945=mmcmxlv 2946=mmcmxlvi 2947=mmcmxlvii 2948=mmcmxlviii 2949=mmcmxlix 2950=mmcml \n" +
                        "2951=mmcmli 2952=mmcmlii 2953=mmcmliii 2954=mmcmliv 2955=mmcmlv 2956=mmcmlvi 2957=mmcmlvii 2958=mmcmlviii 2959=mmcmlix 2960=mmcmlx \n" +
                        "2961=mmcmlxi 2962=mmcmlxii 2963=mmcmlxiii 2964=mmcmlxiv 2965=mmcmlxv 2966=mmcmlxvi 2967=mmcmlxvii 2968=mmcmlxviii 2969=mmcmlxix 2970=mmcmlxx \n" +
                        "2971=mmcmlxxi 2972=mmcmlxxii 2973=mmcmlxxiii 2974=mmcmlxxiv 2975=mmcmlxxv 2976=mmcmlxxvi 2977=mmcmlxxvii 2978=mmcmlxxviii 2979=mmcmlxxix 2980=mmcmlxxx \n" +
                        "2981=mmcmlxxxi 2982=mmcmlxxxii 2983=mmcmlxxxiii 2984=mmcmlxxxiv 2985=mmcmlxxxv 2986=mmcmlxxxvi 2987=mmcmlxxxvii 2988=mmcmlxxxviii 2989=mmcmlxxxix 2990=mmcmxc \n" +
                        "2991=mmcmxci 2992=mmcmxcii 2993=mmcmxciii 2994=mmcmxciv 2995=mmcmxcv 2996=mmcmxcvi 2997=mmcmxcvii 2998=mmcmxcviii 2999=mmcmxcix 3000=mmm \n" +
                        "3001=mmmi 3002=mmmii 3003=mmmiii 3004=mmmiv 3005=mmmv 3006=mmmvi 3007=mmmvii 3008=mmmviii 3009=mmmix 3010=mmmx \n" +
                        "3011=mmmxi 3012=mmmxii 3013=mmmxiii 3014=mmmxiv 3015=mmmxv 3016=mmmxvi 3017=mmmxvii 3018=mmmxviii 3019=mmmxix 3020=mmmxx \n" +
                        "3021=mmmxxi 3022=mmmxxii 3023=mmmxxiii 3024=mmmxxiv 3025=mmmxxv 3026=mmmxxvi 3027=mmmxxvii 3028=mmmxxviii 3029=mmmxxix 3030=mmmxxx \n" +
                        "3031=mmmxxxi 3032=mmmxxxii 3033=mmmxxxiii 3034=mmmxxxiv 3035=mmmxxxv 3036=mmmxxxvi 3037=mmmxxxvii 3038=mmmxxxviii 3039=mmmxxxix 3040=mmmxl \n" +
                        "3041=mmmxli 3042=mmmxlii 3043=mmmxliii 3044=mmmxliv 3045=mmmxlv 3046=mmmxlvi 3047=mmmxlvii 3048=mmmxlviii 3049=mmmxlix 3050=mmml \n" +
                        "3051=mmmli 3052=mmmlii 3053=mmmliii 3054=mmmliv 3055=mmmlv 3056=mmmlvi 3057=mmmlvii 3058=mmmlviii 3059=mmmlix 3060=mmmlx \n" +
                        "3061=mmmlxi 3062=mmmlxii 3063=mmmlxiii 3064=mmmlxiv 3065=mmmlxv 3066=mmmlxvi 3067=mmmlxvii 3068=mmmlxviii 3069=mmmlxix 3070=mmmlxx \n" +
                        "3071=mmmlxxi 3072=mmmlxxii 3073=mmmlxxiii 3074=mmmlxxiv 3075=mmmlxxv 3076=mmmlxxvi 3077=mmmlxxvii 3078=mmmlxxviii 3079=mmmlxxix 3080=mmmlxxx \n" +
                        "3081=mmmlxxxi 3082=mmmlxxxii 3083=mmmlxxxiii 3084=mmmlxxxiv 3085=mmmlxxxv 3086=mmmlxxxvi 3087=mmmlxxxvii 3088=mmmlxxxviii 3089=mmmlxxxix 3090=mmmxc \n" +
                        "3091=mmmxci 3092=mmmxcii 3093=mmmxciii 3094=mmmxciv 3095=mmmxcv 3096=mmmxcvi 3097=mmmxcvii 3098=mmmxcviii 3099=mmmxcix 3100=mmmc \n" +
                        "3101=mmmci 3102=mmmcii 3103=mmmciii 3104=mmmciv 3105=mmmcv 3106=mmmcvi 3107=mmmcvii 3108=mmmcviii 3109=mmmcix 3110=mmmcx \n" +
                        "3111=mmmcxi 3112=mmmcxii 3113=mmmcxiii 3114=mmmcxiv 3115=mmmcxv 3116=mmmcxvi 3117=mmmcxvii 3118=mmmcxviii 3119=mmmcxix 3120=mmmcxx \n" +
                        "3121=mmmcxxi 3122=mmmcxxii 3123=mmmcxxiii 3124=mmmcxxiv 3125=mmmcxxv 3126=mmmcxxvi 3127=mmmcxxvii 3128=mmmcxxviii 3129=mmmcxxix 3130=mmmcxxx \n" +
                        "3131=mmmcxxxi 3132=mmmcxxxii 3133=mmmcxxxiii 3134=mmmcxxxiv 3135=mmmcxxxv 3136=mmmcxxxvi 3137=mmmcxxxvii 3138=mmmcxxxviii 3139=mmmcxxxix 3140=mmmcxl \n" +
                        "3141=mmmcxli 3142=mmmcxlii 3143=mmmcxliii 3144=mmmcxliv 3145=mmmcxlv 3146=mmmcxlvi 3147=mmmcxlvii 3148=mmmcxlviii 3149=mmmcxlix 3150=mmmcl \n" +
                        "3151=mmmcli 3152=mmmclii 3153=mmmcliii 3154=mmmcliv 3155=mmmclv 3156=mmmclvi 3157=mmmclvii 3158=mmmclviii 3159=mmmclix 3160=mmmclx \n" +
                        "3161=mmmclxi 3162=mmmclxii 3163=mmmclxiii 3164=mmmclxiv 3165=mmmclxv 3166=mmmclxvi 3167=mmmclxvii 3168=mmmclxviii 3169=mmmclxix 3170=mmmclxx \n" +
                        "3171=mmmclxxi 3172=mmmclxxii 3173=mmmclxxiii 3174=mmmclxxiv 3175=mmmclxxv 3176=mmmclxxvi 3177=mmmclxxvii 3178=mmmclxxviii 3179=mmmclxxix 3180=mmmclxxx \n" +
                        "3181=mmmclxxxi 3182=mmmclxxxii 3183=mmmclxxxiii 3184=mmmclxxxiv 3185=mmmclxxxv 3186=mmmclxxxvi 3187=mmmclxxxvii 3188=mmmclxxxviii 3189=mmmclxxxix 3190=mmmcxc \n" +
                        "3191=mmmcxci 3192=mmmcxcii 3193=mmmcxciii 3194=mmmcxciv 3195=mmmcxcv 3196=mmmcxcvi 3197=mmmcxcvii 3198=mmmcxcviii 3199=mmmcxcix 3200=mmmcc \n" +
                        "3201=mmmcci 3202=mmmccii 3203=mmmcciii 3204=mmmcciv 3205=mmmccv 3206=mmmccvi 3207=mmmccvii 3208=mmmccviii 3209=mmmccix 3210=mmmccx \n" +
                        "3211=mmmccxi 3212=mmmccxii 3213=mmmccxiii 3214=mmmccxiv 3215=mmmccxv 3216=mmmccxvi 3217=mmmccxvii 3218=mmmccxviii 3219=mmmccxix 3220=mmmccxx \n" +
                        "3221=mmmccxxi 3222=mmmccxxii 3223=mmmccxxiii 3224=mmmccxxiv 3225=mmmccxxv 3226=mmmccxxvi 3227=mmmccxxvii 3228=mmmccxxviii 3229=mmmccxxix 3230=mmmccxxx \n" +
                        "3231=mmmccxxxi 3232=mmmccxxxii 3233=mmmccxxxiii 3234=mmmccxxxiv 3235=mmmccxxxv 3236=mmmccxxxvi 3237=mmmccxxxvii 3238=mmmccxxxviii 3239=mmmccxxxix 3240=mmmccxl \n" +
                        "3241=mmmccxli 3242=mmmccxlii 3243=mmmccxliii 3244=mmmccxliv 3245=mmmccxlv 3246=mmmccxlvi 3247=mmmccxlvii 3248=mmmccxlviii 3249=mmmccxlix 3250=mmmccl \n" +
                        "3251=mmmccli 3252=mmmcclii 3253=mmmccliii 3254=mmmccliv 3255=mmmcclv 3256=mmmcclvi 3257=mmmcclvii 3258=mmmcclviii 3259=mmmcclix 3260=mmmcclx \n" +
                        "3261=mmmcclxi 3262=mmmcclxii 3263=mmmcclxiii 3264=mmmcclxiv 3265=mmmcclxv 3266=mmmcclxvi 3267=mmmcclxvii 3268=mmmcclxviii 3269=mmmcclxix 3270=mmmcclxx \n" +
                        "3271=mmmcclxxi 3272=mmmcclxxii 3273=mmmcclxxiii 3274=mmmcclxxiv 3275=mmmcclxxv 3276=mmmcclxxvi 3277=mmmcclxxvii 3278=mmmcclxxviii 3279=mmmcclxxix 3280=mmmcclxxx \n" +
                        "3281=mmmcclxxxi 3282=mmmcclxxxii 3283=mmmcclxxxiii 3284=mmmcclxxxiv 3285=mmmcclxxxv 3286=mmmcclxxxvi 3287=mmmcclxxxvii 3288=mmmcclxxxviii 3289=mmmcclxxxix 3290=mmmccxc \n" +
                        "3291=mmmccxci 3292=mmmccxcii 3293=mmmccxciii 3294=mmmccxciv 3295=mmmccxcv 3296=mmmccxcvi 3297=mmmccxcvii 3298=mmmccxcviii 3299=mmmccxcix 3300=mmmccc \n" +
                        "3301=mmmccci 3302=mmmcccii 3303=mmmccciii 3304=mmmccciv 3305=mmmcccv 3306=mmmcccvi 3307=mmmcccvii 3308=mmmcccviii 3309=mmmcccix 3310=mmmcccx \n" +
                        "3311=mmmcccxi 3312=mmmcccxii 3313=mmmcccxiii 3314=mmmcccxiv 3315=mmmcccxv 3316=mmmcccxvi 3317=mmmcccxvii 3318=mmmcccxviii 3319=mmmcccxix 3320=mmmcccxx \n" +
                        "3321=mmmcccxxi 3322=mmmcccxxii 3323=mmmcccxxiii 3324=mmmcccxxiv 3325=mmmcccxxv 3326=mmmcccxxvi 3327=mmmcccxxvii 3328=mmmcccxxviii 3329=mmmcccxxix 3330=mmmcccxxx \n" +
                        "3331=mmmcccxxxi 3332=mmmcccxxxii 3333=mmmcccxxxiii 3334=mmmcccxxxiv 3335=mmmcccxxxv 3336=mmmcccxxxvi 3337=mmmcccxxxvii 3338=mmmcccxxxviii 3339=mmmcccxxxix 3340=mmmcccxl \n" +
                        "3341=mmmcccxli 3342=mmmcccxlii 3343=mmmcccxliii 3344=mmmcccxliv 3345=mmmcccxlv 3346=mmmcccxlvi 3347=mmmcccxlvii 3348=mmmcccxlviii 3349=mmmcccxlix 3350=mmmcccl \n" +
                        "3351=mmmcccli 3352=mmmccclii 3353=mmmcccliii 3354=mmmcccliv 3355=mmmccclv 3356=mmmccclvi 3357=mmmccclvii 3358=mmmccclviii 3359=mmmccclix 3360=mmmccclx \n" +
                        "3361=mmmccclxi 3362=mmmccclxii 3363=mmmccclxiii 3364=mmmccclxiv 3365=mmmccclxv 3366=mmmccclxvi 3367=mmmccclxvii 3368=mmmccclxviii 3369=mmmccclxix 3370=mmmccclxx \n" +
                        "3371=mmmccclxxi 3372=mmmccclxxii 3373=mmmccclxxiii 3374=mmmccclxxiv 3375=mmmccclxxv 3376=mmmccclxxvi 3377=mmmccclxxvii 3378=mmmccclxxviii 3379=mmmccclxxix 3380=mmmccclxxx \n" +
                        "3381=mmmccclxxxi 3382=mmmccclxxxii 3383=mmmccclxxxiii 3384=mmmccclxxxiv 3385=mmmccclxxxv 3386=mmmccclxxxvi 3387=mmmccclxxxvii 3388=mmmccclxxxviii 3389=mmmccclxxxix 3390=mmmcccxc \n" +
                        "3391=mmmcccxci 3392=mmmcccxcii 3393=mmmcccxciii 3394=mmmcccxciv 3395=mmmcccxcv 3396=mmmcccxcvi 3397=mmmcccxcvii 3398=mmmcccxcviii 3399=mmmcccxcix 3400=mmmcd \n" +
                        "3401=mmmcdi 3402=mmmcdii 3403=mmmcdiii 3404=mmmcdiv 3405=mmmcdv 3406=mmmcdvi 3407=mmmcdvii 3408=mmmcdviii 3409=mmmcdix 3410=mmmcdx \n" +
                        "3411=mmmcdxi 3412=mmmcdxii 3413=mmmcdxiii 3414=mmmcdxiv 3415=mmmcdxv 3416=mmmcdxvi 3417=mmmcdxvii 3418=mmmcdxviii 3419=mmmcdxix 3420=mmmcdxx \n" +
                        "3421=mmmcdxxi 3422=mmmcdxxii 3423=mmmcdxxiii 3424=mmmcdxxiv 3425=mmmcdxxv 3426=mmmcdxxvi 3427=mmmcdxxvii 3428=mmmcdxxviii 3429=mmmcdxxix 3430=mmmcdxxx \n" +
                        "3431=mmmcdxxxi 3432=mmmcdxxxii 3433=mmmcdxxxiii 3434=mmmcdxxxiv 3435=mmmcdxxxv 3436=mmmcdxxxvi 3437=mmmcdxxxvii 3438=mmmcdxxxviii 3439=mmmcdxxxix 3440=mmmcdxl \n" +
                        "3441=mmmcdxli 3442=mmmcdxlii 3443=mmmcdxliii 3444=mmmcdxliv 3445=mmmcdxlv 3446=mmmcdxlvi 3447=mmmcdxlvii 3448=mmmcdxlviii 3449=mmmcdxlix 3450=mmmcdl \n" +
                        "3451=mmmcdli 3452=mmmcdlii 3453=mmmcdliii 3454=mmmcdliv 3455=mmmcdlv 3456=mmmcdlvi 3457=mmmcdlvii 3458=mmmcdlviii 3459=mmmcdlix 3460=mmmcdlx \n" +
                        "3461=mmmcdlxi 3462=mmmcdlxii 3463=mmmcdlxiii 3464=mmmcdlxiv 3465=mmmcdlxv 3466=mmmcdlxvi 3467=mmmcdlxvii 3468=mmmcdlxviii 3469=mmmcdlxix 3470=mmmcdlxx \n" +
                        "3471=mmmcdlxxi 3472=mmmcdlxxii 3473=mmmcdlxxiii 3474=mmmcdlxxiv 3475=mmmcdlxxv 3476=mmmcdlxxvi 3477=mmmcdlxxvii 3478=mmmcdlxxviii 3479=mmmcdlxxix 3480=mmmcdlxxx \n" +
                        "3481=mmmcdlxxxi 3482=mmmcdlxxxii 3483=mmmcdlxxxiii 3484=mmmcdlxxxiv 3485=mmmcdlxxxv 3486=mmmcdlxxxvi 3487=mmmcdlxxxvii 3488=mmmcdlxxxviii 3489=mmmcdlxxxix 3490=mmmcdxc \n" +
                        "3491=mmmcdxci 3492=mmmcdxcii 3493=mmmcdxciii 3494=mmmcdxciv 3495=mmmcdxcv 3496=mmmcdxcvi 3497=mmmcdxcvii 3498=mmmcdxcviii 3499=mmmcdxcix 3500=mmmd \n" +
                        "3501=mmmdi 3502=mmmdii 3503=mmmdiii 3504=mmmdiv 3505=mmmdv 3506=mmmdvi 3507=mmmdvii 3508=mmmdviii 3509=mmmdix 3510=mmmdx \n" +
                        "3511=mmmdxi 3512=mmmdxii 3513=mmmdxiii 3514=mmmdxiv 3515=mmmdxv 3516=mmmdxvi 3517=mmmdxvii 3518=mmmdxviii 3519=mmmdxix 3520=mmmdxx \n" +
                        "3521=mmmdxxi 3522=mmmdxxii 3523=mmmdxxiii 3524=mmmdxxiv 3525=mmmdxxv 3526=mmmdxxvi 3527=mmmdxxvii 3528=mmmdxxviii 3529=mmmdxxix 3530=mmmdxxx \n" +
                        "3531=mmmdxxxi 3532=mmmdxxxii 3533=mmmdxxxiii 3534=mmmdxxxiv 3535=mmmdxxxv 3536=mmmdxxxvi 3537=mmmdxxxvii 3538=mmmdxxxviii 3539=mmmdxxxix 3540=mmmdxl \n" +
                        "3541=mmmdxli 3542=mmmdxlii 3543=mmmdxliii 3544=mmmdxliv 3545=mmmdxlv 3546=mmmdxlvi 3547=mmmdxlvii 3548=mmmdxlviii 3549=mmmdxlix 3550=mmmdl \n" +
                        "3551=mmmdli 3552=mmmdlii 3553=mmmdliii 3554=mmmdliv 3555=mmmdlv 3556=mmmdlvi 3557=mmmdlvii 3558=mmmdlviii 3559=mmmdlix 3560=mmmdlx \n" +
                        "3561=mmmdlxi 3562=mmmdlxii 3563=mmmdlxiii 3564=mmmdlxiv 3565=mmmdlxv 3566=mmmdlxvi 3567=mmmdlxvii 3568=mmmdlxviii 3569=mmmdlxix 3570=mmmdlxx \n" +
                        "3571=mmmdlxxi 3572=mmmdlxxii 3573=mmmdlxxiii 3574=mmmdlxxiv 3575=mmmdlxxv 3576=mmmdlxxvi 3577=mmmdlxxvii 3578=mmmdlxxviii 3579=mmmdlxxix 3580=mmmdlxxx \n" +
                        "3581=mmmdlxxxi 3582=mmmdlxxxii 3583=mmmdlxxxiii 3584=mmmdlxxxiv 3585=mmmdlxxxv 3586=mmmdlxxxvi 3587=mmmdlxxxvii 3588=mmmdlxxxviii 3589=mmmdlxxxix 3590=mmmdxc \n" +
                        "3591=mmmdxci 3592=mmmdxcii 3593=mmmdxciii 3594=mmmdxciv 3595=mmmdxcv 3596=mmmdxcvi 3597=mmmdxcvii 3598=mmmdxcviii 3599=mmmdxcix 3600=mmmdc \n" +
                        "3601=mmmdci 3602=mmmdcii 3603=mmmdciii 3604=mmmdciv 3605=mmmdcv 3606=mmmdcvi 3607=mmmdcvii 3608=mmmdcviii 3609=mmmdcix 3610=mmmdcx \n" +
                        "3611=mmmdcxi 3612=mmmdcxii 3613=mmmdcxiii 3614=mmmdcxiv 3615=mmmdcxv 3616=mmmdcxvi 3617=mmmdcxvii 3618=mmmdcxviii 3619=mmmdcxix 3620=mmmdcxx \n" +
                        "3621=mmmdcxxi 3622=mmmdcxxii 3623=mmmdcxxiii 3624=mmmdcxxiv 3625=mmmdcxxv 3626=mmmdcxxvi 3627=mmmdcxxvii 3628=mmmdcxxviii 3629=mmmdcxxix 3630=mmmdcxxx \n" +
                        "3631=mmmdcxxxi 3632=mmmdcxxxii 3633=mmmdcxxxiii 3634=mmmdcxxxiv 3635=mmmdcxxxv 3636=mmmdcxxxvi 3637=mmmdcxxxvii 3638=mmmdcxxxviii 3639=mmmdcxxxix 3640=mmmdcxl \n" +
                        "3641=mmmdcxli 3642=mmmdcxlii 3643=mmmdcxliii 3644=mmmdcxliv 3645=mmmdcxlv 3646=mmmdcxlvi 3647=mmmdcxlvii 3648=mmmdcxlviii 3649=mmmdcxlix 3650=mmmdcl \n" +
                        "3651=mmmdcli 3652=mmmdclii 3653=mmmdcliii 3654=mmmdcliv 3655=mmmdclv 3656=mmmdclvi 3657=mmmdclvii 3658=mmmdclviii 3659=mmmdclix 3660=mmmdclx \n" +
                        "3661=mmmdclxi 3662=mmmdclxii 3663=mmmdclxiii 3664=mmmdclxiv 3665=mmmdclxv 3666=mmmdclxvi 3667=mmmdclxvii 3668=mmmdclxviii 3669=mmmdclxix 3670=mmmdclxx \n" +
                        "3671=mmmdclxxi 3672=mmmdclxxii 3673=mmmdclxxiii 3674=mmmdclxxiv 3675=mmmdclxxv 3676=mmmdclxxvi 3677=mmmdclxxvii 3678=mmmdclxxviii 3679=mmmdclxxix 3680=mmmdclxxx \n" +
                        "3681=mmmdclxxxi 3682=mmmdclxxxii 3683=mmmdclxxxiii 3684=mmmdclxxxiv 3685=mmmdclxxxv 3686=mmmdclxxxvi 3687=mmmdclxxxvii 3688=mmmdclxxxviii 3689=mmmdclxxxix 3690=mmmdcxc \n" +
                        "3691=mmmdcxci 3692=mmmdcxcii 3693=mmmdcxciii 3694=mmmdcxciv 3695=mmmdcxcv 3696=mmmdcxcvi 3697=mmmdcxcvii 3698=mmmdcxcviii 3699=mmmdcxcix 3700=mmmdcc \n" +
                        "3701=mmmdcci 3702=mmmdccii 3703=mmmdcciii 3704=mmmdcciv 3705=mmmdccv 3706=mmmdccvi 3707=mmmdccvii 3708=mmmdccviii 3709=mmmdccix 3710=mmmdccx \n" +
                        "3711=mmmdccxi 3712=mmmdccxii 3713=mmmdccxiii 3714=mmmdccxiv 3715=mmmdccxv 3716=mmmdccxvi 3717=mmmdccxvii 3718=mmmdccxviii 3719=mmmdccxix 3720=mmmdccxx \n" +
                        "3721=mmmdccxxi 3722=mmmdccxxii 3723=mmmdccxxiii 3724=mmmdccxxiv 3725=mmmdccxxv 3726=mmmdccxxvi 3727=mmmdccxxvii 3728=mmmdccxxviii 3729=mmmdccxxix 3730=mmmdccxxx \n" +
                        "3731=mmmdccxxxi 3732=mmmdccxxxii 3733=mmmdccxxxiii 3734=mmmdccxxxiv 3735=mmmdccxxxv 3736=mmmdccxxxvi 3737=mmmdccxxxvii 3738=mmmdccxxxviii 3739=mmmdccxxxix 3740=mmmdccxl \n" +
                        "3741=mmmdccxli 3742=mmmdccxlii 3743=mmmdccxliii 3744=mmmdccxliv 3745=mmmdccxlv 3746=mmmdccxlvi 3747=mmmdccxlvii 3748=mmmdccxlviii 3749=mmmdccxlix 3750=mmmdccl \n" +
                        "3751=mmmdccli 3752=mmmdcclii 3753=mmmdccliii 3754=mmmdccliv 3755=mmmdcclv 3756=mmmdcclvi 3757=mmmdcclvii 3758=mmmdcclviii 3759=mmmdcclix 3760=mmmdcclx \n" +
                        "3761=mmmdcclxi 3762=mmmdcclxii 3763=mmmdcclxiii 3764=mmmdcclxiv 3765=mmmdcclxv 3766=mmmdcclxvi 3767=mmmdcclxvii 3768=mmmdcclxviii 3769=mmmdcclxix 3770=mmmdcclxx \n" +
                        "3771=mmmdcclxxi 3772=mmmdcclxxii 3773=mmmdcclxxiii 3774=mmmdcclxxiv 3775=mmmdcclxxv 3776=mmmdcclxxvi 3777=mmmdcclxxvii 3778=mmmdcclxxviii 3779=mmmdcclxxix 3780=mmmdcclxxx \n" +
                        "3781=mmmdcclxxxi 3782=mmmdcclxxxii 3783=mmmdcclxxxiii 3784=mmmdcclxxxiv 3785=mmmdcclxxxv 3786=mmmdcclxxxvi 3787=mmmdcclxxxvii 3788=mmmdcclxxxviii 3789=mmmdcclxxxix 3790=mmmdccxc \n" +
                        "3791=mmmdccxci 3792=mmmdccxcii 3793=mmmdccxciii 3794=mmmdccxciv 3795=mmmdccxcv 3796=mmmdccxcvi 3797=mmmdccxcvii 3798=mmmdccxcviii 3799=mmmdccxcix 3800=mmmdccc \n" +
                        "3801=mmmdccci 3802=mmmdcccii 3803=mmmdccciii 3804=mmmdccciv 3805=mmmdcccv 3806=mmmdcccvi 3807=mmmdcccvii 3808=mmmdcccviii 3809=mmmdcccix 3810=mmmdcccx \n" +
                        "3811=mmmdcccxi 3812=mmmdcccxii 3813=mmmdcccxiii 3814=mmmdcccxiv 3815=mmmdcccxv 3816=mmmdcccxvi 3817=mmmdcccxvii 3818=mmmdcccxviii 3819=mmmdcccxix 3820=mmmdcccxx \n" +
                        "3821=mmmdcccxxi 3822=mmmdcccxxii 3823=mmmdcccxxiii 3824=mmmdcccxxiv 3825=mmmdcccxxv 3826=mmmdcccxxvi 3827=mmmdcccxxvii 3828=mmmdcccxxviii 3829=mmmdcccxxix 3830=mmmdcccxxx \n" +
                        "3831=mmmdcccxxxi 3832=mmmdcccxxxii 3833=mmmdcccxxxiii 3834=mmmdcccxxxiv 3835=mmmdcccxxxv 3836=mmmdcccxxxvi 3837=mmmdcccxxxvii 3838=mmmdcccxxxviii 3839=mmmdcccxxxix 3840=mmmdcccxl \n" +
                        "3841=mmmdcccxli 3842=mmmdcccxlii 3843=mmmdcccxliii 3844=mmmdcccxliv 3845=mmmdcccxlv 3846=mmmdcccxlvi 3847=mmmdcccxlvii 3848=mmmdcccxlviii 3849=mmmdcccxlix 3850=mmmdcccl \n" +
                        "3851=mmmdcccli 3852=mmmdccclii 3853=mmmdcccliii 3854=mmmdcccliv 3855=mmmdccclv 3856=mmmdccclvi 3857=mmmdccclvii 3858=mmmdccclviii 3859=mmmdccclix 3860=mmmdccclx \n" +
                        "3861=mmmdccclxi 3862=mmmdccclxii 3863=mmmdccclxiii 3864=mmmdccclxiv 3865=mmmdccclxv 3866=mmmdccclxvi 3867=mmmdccclxvii 3868=mmmdccclxviii 3869=mmmdccclxix 3870=mmmdccclxx \n" +
                        "3871=mmmdccclxxi 3872=mmmdccclxxii 3873=mmmdccclxxiii 3874=mmmdccclxxiv 3875=mmmdccclxxv 3876=mmmdccclxxvi 3877=mmmdccclxxvii 3878=mmmdccclxxviii 3879=mmmdccclxxix 3880=mmmdccclxxx \n" +
                        "3881=mmmdccclxxxi 3882=mmmdccclxxxii 3883=mmmdccclxxxiii 3884=mmmdccclxxxiv 3885=mmmdccclxxxv 3886=mmmdccclxxxvi 3887=mmmdccclxxxvii 3888=mmmdccclxxxviii 3889=mmmdccclxxxix 3890=mmmdcccxc \n" +
                        "3891=mmmdcccxci 3892=mmmdcccxcii 3893=mmmdcccxciii 3894=mmmdcccxciv 3895=mmmdcccxcv 3896=mmmdcccxcvi 3897=mmmdcccxcvii 3898=mmmdcccxcviii 3899=mmmdcccxcix 3900=mmmcm \n" +
                        "3901=mmmcmi 3902=mmmcmii 3903=mmmcmiii 3904=mmmcmiv 3905=mmmcmv 3906=mmmcmvi 3907=mmmcmvii 3908=mmmcmviii 3909=mmmcmix 3910=mmmcmx \n" +
                        "3911=mmmcmxi 3912=mmmcmxii 3913=mmmcmxiii 3914=mmmcmxiv 3915=mmmcmxv 3916=mmmcmxvi 3917=mmmcmxvii 3918=mmmcmxviii 3919=mmmcmxix 3920=mmmcmxx \n" +
                        "3921=mmmcmxxi 3922=mmmcmxxii 3923=mmmcmxxiii 3924=mmmcmxxiv 3925=mmmcmxxv 3926=mmmcmxxvi 3927=mmmcmxxvii 3928=mmmcmxxviii 3929=mmmcmxxix 3930=mmmcmxxx \n" +
                        "3931=mmmcmxxxi 3932=mmmcmxxxii 3933=mmmcmxxxiii 3934=mmmcmxxxiv 3935=mmmcmxxxv 3936=mmmcmxxxvi 3937=mmmcmxxxvii 3938=mmmcmxxxviii 3939=mmmcmxxxix 3940=mmmcmxl \n" +
                        "3941=mmmcmxli 3942=mmmcmxlii 3943=mmmcmxliii 3944=mmmcmxliv 3945=mmmcmxlv 3946=mmmcmxlvi 3947=mmmcmxlvii 3948=mmmcmxlviii 3949=mmmcmxlix 3950=mmmcml \n" +
                        "3951=mmmcmli 3952=mmmcmlii 3953=mmmcmliii 3954=mmmcmliv 3955=mmmcmlv 3956=mmmcmlvi 3957=mmmcmlvii 3958=mmmcmlviii 3959=mmmcmlix 3960=mmmcmlx \n" +
                        "3961=mmmcmlxi 3962=mmmcmlxii 3963=mmmcmlxiii 3964=mmmcmlxiv 3965=mmmcmlxv 3966=mmmcmlxvi 3967=mmmcmlxvii 3968=mmmcmlxviii 3969=mmmcmlxix 3970=mmmcmlxx \n" +
                        "3971=mmmcmlxxi 3972=mmmcmlxxii 3973=mmmcmlxxiii 3974=mmmcmlxxiv 3975=mmmcmlxxv 3976=mmmcmlxxvi 3977=mmmcmlxxvii 3978=mmmcmlxxviii 3979=mmmcmlxxix 3980=mmmcmlxxx \n" +
                        "3981=mmmcmlxxxi 3982=mmmcmlxxxii 3983=mmmcmlxxxiii 3984=mmmcmlxxxiv 3985=mmmcmlxxxv 3986=mmmcmlxxxvi 3987=mmmcmlxxxvii 3988=mmmcmlxxxviii 3989=mmmcmlxxxix 3990=mmmcmxc \n" +
                        "3991=mmmcmxci 3992=mmmcmxcii 3993=mmmcmxciii 3994=mmmcmxciv 3995=mmmcmxcv 3996=mmmcmxcvi 3997=mmmcmxcvii 3998=mmmcmxcviii 3999=mmmcmxcix "
                //</editor-fold>
        );
    }

    @Test
    @DisplayName("Formatting as latin")
    void testLatin() throws Exception {
        TestThat.theInput("{#counter:define format=$latin id=f}{f}{f}{f}{f}"
        ).results("abcd");
    }

    @Test
    @DisplayName("Formatting as LATIN")
    void testLATIN() throws Exception {
        TestThat.theInput("{#counter:define format=$LATIN id=f}{f}{f}{f}{f}"
        ).results("ABCD");
    }

    @Test
    @DisplayName("Formatting as greek")
    void testGreek() throws Exception {
        TestThat.theInput("{#counter:define format=$greek id=f}{f}{f}{f}{f}"
        ).results("αβγδ");
    }

    @Test
    @DisplayName("Formatting as GREEK")
    void testGREEK() throws Exception {
        TestThat.theInput("{#counter:define format=$GREEK id=f}{f}{f}{f}{f}"
        ).results("ΑΒΓΔ");
    }    @Test
    @DisplayName("Formatting as cyrillic")
    void testCyrillic() throws Exception {
        TestThat.theInput("{#counter:define format=$cyrillic id=f}{f}{f}{f}{f}"
        ).results("абвг");
    }

    @Test
    @DisplayName("Formatting as CIRILLIC")
    void testCYRILLIC() throws Exception {
        TestThat.theInput("{#counter:define format=$CYRILLIC id=f}{f}{f}{f}{f}"
        ).results("АБВГ");
    }


}
