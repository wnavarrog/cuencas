
%plot events
clear

%input:% Event_selection - sites info
%        Event_list2 - list of events
%        Event_Qmax.txt - contain max Q per event per site (DIT Event_max to read in the next step)
% output: plot of the events
nday=7
SWE=10;
identifier=['snow_' int2str(nday) 'day'];
Ebegin= datenum(2010, 01,01, 0, 0, 0);
Eend = Ebegin+nday;
flagday=1; %zero if melt occurs trhough the day/night
time_res_min= 60;    % number of days

nfiles=ceil(((Eend-Ebegin)*60*24)/(time_res_min));
MINLAT=41;
MAXLAT=44;
MINLON=-94;
MAXLON=-90;

RES=floor(15*60) % in (ArcSec)  [dec deg]: 0.016667*60 - radar info
                % satellite 15*60
iRES=int16(RES);                
ncol=int16(ceil(abs((MAXLON-MINLON)/(RES/3600))));
nrows=int16(ceil(abs((MAXLAT-MINLAT)/(RES/3660))));

outputdirasc=['E:\CUENCAS\CedarRapids\Rainfall\sintetic_rainfall\constant\' num2str(SWE) '\'  int2str(nday) 'day_6h\asc\']
outputdirvhc=['E:\CUENCAS\CedarRapids\Rainfall\sintetic_rainfall\constant\' num2str(SWE) '\'  int2str(nday) 'day_6h\vhc\']
mkdir(outputdirasc)
mkdir(outputdirvhc)
% MODE CONSTANT PRECIPITATION
% calculate constant precipitation rate mm/h

     for ifi=1:nfiles  
        %file name
        dat=datestr((Ebegin+(time_res_min*ifi/(24*60))),'.HHMMSS.dd.mmmm.yyyy')
        
        filename=[outputdirasc identifier dat '.asc']
        % estimate precipitation
        if (flagday==1) 
        A=datestr((Ebegin+(time_res_min*ifi/(24*60))),'HH')
        hhh = sscanf(A,'%d')
        for ir=1:nrows
           for ic=1:ncol
               if(hhh>9 && hhh<15) Z(ir,ic)=SWE/((Eend-Ebegin)*6);
               else Z(ir,ic)=0.0;
               end
           end
        end
        else
        for ir=1:nrows
           for ic=1:ncol
               Z(ir,ic)=SWE/((Eend-Ebegin)*24);
           end
        end   
        end

        filename=[outputdirasc identifier dat '.asc']
        Gener_Asc(filename,RES,ncol,nrows,MINLON,MINLAT,Z)
        filename=[outputdirvhc identifier dat '.vhc']  
        Gener_Vhc(filename,RES,ncol,nrows,MINLON,MINLAT,Z) 
    end


% GENERATE METADATA

            fid=fopen([outputdirvhc identifier '.metaVHC'],'w');
            
            fprintf(fid,'[Name]\n');
            
            fprintf(fid,'Sintetic precipitation to test snow melt\n');
            fprintf(fid,'[Southernmost Latitude]\n');
            deg=floor(MINLAT);
            ideg=int16(deg);
            min=floor((MINLAT-deg)*60);
            imin=int16(min);
            sec=(MINLAT-deg)*60-(floor((MINLAT-deg)*60));
            
            fprintf(fid,'%02d:%02d:%02.2f N\n',ideg,imin,sec);
            clear deg ideg min imin sec
            fprintf(fid,'[Westernmost Longitude]\n');
            deg=floor(MINLON);
            ideg=int16(deg);
            min=floor((MINLON-deg)*60);
            imin=int16(min);
            sec=(MINLON-deg)*60-(floor((MINLON-deg)*60));
            fprintf(fid,'%02d:%02d:%02.2f W\n',-ideg,imin,sec);      
            fprintf(fid,'[Longitudinal Resolution (ArcSec)]\n');
            fprintf(fid,'%d\n',iRES);
            fprintf(fid,'[Latitudinal Resolution (ArcSec)]\n');
            fprintf(fid,'%d\n',iRES);
            fprintf(fid,'[# Columns]\n');
            fprintf(fid,'%d\n',ncol);
            fprintf(fid,'[# Rows]\n');
            fprintf(fid,'%d\n',nrows);
            fprintf(fid,'[Format]\n');
            fprintf(fid,'Float\n');
            fprintf(fid,'[Missing]\n');
            fprintf(fid,'-9.00\n');
            fprintf(fid,'[Temporal Resolution]\n');         
            fprintf(fid,'%d-minutes\n',time_res_min);
            fprintf(fid,'[Units]\n');
            fprintf(fid,'mm/h\n');
            fprintf(fid,'[Information]\n');
            fprintf(fid,'Sintetic precipitation to test snow melt\n');
            fclose(fid);
            
            status = fclose('all')
            