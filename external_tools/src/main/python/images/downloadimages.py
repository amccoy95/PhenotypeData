#!/usr/bin/python

#this program gets the download_file_paths (http mousephenotype uris) from the experiment core and then downloads the images 
import os
import requests
import json
import sys
import os.path
import sys
import argparse
from common import splitString
from OmeroPropertiesParser import OmeroPropertiesParser

responseFailed=0
numberOfImageDownloadAttemps=0
totalNumberOfImagesWeHave=0
numFoundInSolr=0


### REMOVE ###
fname = "/home/kola/code/embl_projects/PhenotypeData/external_tools/src/main/resources/harwell_old_url_to_new_url_map.txt"
with open(fname,'rt') as fid:                                          
    url_map = {l.split()[0]:l.split()[1] for l in fid.readlines()}

uniqueUris=set()

def main(argv):
    """Download images referred to in solr index onto holding area on disk"""
    
    parser = argparse.ArgumentParser(
        description='Download images referred to in solr index onto holding area on disk'
    )
    parser.add_argument('-d', '--rootDestinationDir', dest='rootDestinationDir',
                        help='Directory for root of destination to store images'    )
    parser.add_argument('-s', '--rootSolrUrl', dest='rootSolrUrl',
                        help='URL to root of solr index'
    )
    parser.add_argument('-p', '--port', dest='komp2Port',
                        help='Port by which to connect to komp2 db'
    )
    parser.add_argument('-u', '--user', dest='komp2User',
                        help='Username for connecting to komp2 db'
    )
    parser.add_argument('-db', '--database', dest='komp2Db',
                        help='Database to connect to for komp2db'
    )
    parser.add_argument('--not-downloaded', dest='notDownloadedOutputPath',
                        help='path to save list of files that could not be downloaded'
    )
    parser.add_argument('--pass', dest='komp2Pass',
                        help='Password for komp2db'
    )
    parser.add_argument('--profile', dest='profile', default='dev',
                        help='profile from which to read config: dev, prod or live')
    parser.add_argument('--profile-path', dest='profilePath',
                        help='Explicit path to file from which to read ' + \
                             'profile e.g. ' + \
                             '/home/kola/application.properties. ' + \
                             'Overrides value of --profile argument.'
    )
    args = parser.parse_args()
    
    # Get values from property file and use as defaults that can be overridden
    # by command line parameters
    if args.profilePath is not None:
        try:
            pp = OmeroPropertiesParser()
            omeroProps = pp.getOmeroProps(args.profilePath)
        except Exception as e:
            print "Could not read application properties file from " + args.profilePath
            print "Error was: " + str(e)
            return
    else:
        try:
            pp = OmeroPropertiesParser(args.profile)
            omeroProps = pp.getOmeroProps()
        except Exception as e:
            print "Could not read application properties file for profile " + args.profile
            print "Error was: " + str(e)
            return

    rootSolrUrl = args.rootSolrUrl if args.rootSolrUrl<>None else omeroProps['solrurl']
    #solrQuery="""experiment/select?q=observation_type:image_record&fq=download_file_path:(download_file_path:*bhjlk01.jax.org/images/IMPC_ALZ_001/*%20AND%20!download_file_path:*.mov)&fl=id,download_file_path,phenotyping_center,pipeline_stable_id,procedure_stable_id,datasource_name,parameter_stable_id&wt=json&indent=on&rows=10000000"""
    #solrQuery="""experiment/select?q=observation_type:image_record&fq=(download_file_path:*mousephenotype.org*%20AND%20!download_file_path:*.mov%20AND%20!download_file_path:*.bz2)&fl=id,download_file_path,phenotyping_center,pipeline_stable_id,procedure_stable_id,datasource_name,parameter_stable_id&wt=json&indent=on&rows=10000000"""
    solrQuery="""experiment/select?q=observation_type:image_record&fq=(download_file_path:*mousephenotype.org*%20AND%20!download_file_path:*.mov%20AND%20!download_file_path:*.bz2)&fl=id,download_file_path,phenotyping_center,pipeline_stable_id,procedure_stable_id,datasource_name,parameter_stable_id&wt=json&indent=on&rows=10"""

    #note cant split this url over a few lines as puts in newlines into url which doesn't work
    rootDestinationDir = args.rootDestinationDir if args.rootDestinationDir<>None else omeroProps['rootdestinationdir'] 
    print "running python image download script for impc images"

    print 'rootDestinationDir is "', rootDestinationDir
    solrUrl=rootSolrUrl+solrQuery;
    print 'solrUrl', solrUrl

    notDownloaded = runWithSolrAsDataSource(solrUrl, rootDestinationDir)
    notDownloadedOutputPath =  args.notDownloadedOutputPath if args.notDownloadedOutputPath <> None else createNotDownloadedOutputPath(rootDestinationDir)
    
    with open(notDownloadedOutputPath, 'wt') as fid:
        fid.writelines(notDownloaded)

    print str(len(notDownloaded)) + " files could not be downloaded. Written to " + notDownloadedOutputPath

def runWithSolrAsDataSource(solrUrl, rootDestinationDir):
    """
        Download images using Solr as the datasource.
        Return urls that cannot be downloaded
    """

    ### REMOVE ###
    global url_map

    notDownloaded = []
    v = json.loads(requests.get(solrUrl).text)
    docs=v['response']['docs']
    numFoundInSolr=v['response']['numFound']
    for doc in docs:
        download_file_path=doc['download_file_path']
        download_file_path=download_file_path.lower()
        ### REMOVE ###
        to_replace = os.path.split(download_file_path)[0] + "/"
        to_replace = to_replace.replace('http://images.mousephenotype.org','')
        download_file_path =  download_file_path.replace(to_replace, url_map[to_replace])
        datasource_id=doc['datasource_name']
        phenotyping_center=doc['phenotyping_center']
        #experiment=doc['experiment']
        pipeline_stable_id=doc['pipeline_stable_id']
        observation_id=doc['id']
        procedure_stable_id=doc['procedure_stable_id']
        parameter_stable_id=doc['parameter_stable_id']
        downloaded = processFile(observation_id, rootDestinationDir,phenotyping_center,pipeline_stable_id, procedure_stable_id, parameter_stable_id, download_file_path)
        if not downloaded:
            notDownloaded.append(download_file_path+'\n')
        
    print 'number found in solr='+str(numFoundInSolr)+' number of failed responses='+str(responseFailed)+' number of requests='+str(numberOfImageDownloadAttemps)+' total totalNumberOfImagesWeHave='+str(totalNumberOfImagesWeHave)
    return notDownloaded

def createDestinationFilePath(rootDestinationDir, phenotyping_center, pipeline_stable_id, procedure, parameter, download_file_path):
    directory="/".join([rootDestinationDir,phenotyping_center, pipeline_stable_id,procedure,parameter])
    return directory

def createNotDownloadedOutputPath(rootDestinationDir):
    """
        Create path to store files that could not be downloaded.
        This uses the current date and time and the root destination dir
    """
    
    import time
    from datetime import datetime

    today = datetime.fromtimestamp(time.time())
    return os.path.join(rootDestinationDir, today.strftime("%Y%m%d_%H%M%S_could_not_download.txt"))

def processFile(observation_id,  rootDestinationDir, phenotyping_center,pipeline_stable_id, procedure, parameter, downloadFilePath):
    global totalNumberOfImagesWeHave
    global responseFailed
    global numberOfImageDownloadAttemps
    directory = createDestinationFilePath(rootDestinationDir, phenotyping_center, pipeline_stable_id, procedure,parameter, downloadFilePath)
    dstfilename=directory+"/"+str(downloadFilePath.split('/')[-1])
    if dstfilename in uniqueUris:
        print '---------------------!!!!!!!!!!error the filePath is not unique and has been specified before:'+dstfilename
    uniqueUris.add(dstfilename)
    fullResolutionFilePath=dstfilename.split(splitString,1)[1]
    #print 'saving file to '+dstfilename
    if os.path.isfile(dstfilename):
        #print "file already here"
        totalNumberOfImagesWeHave+=1
    else:
        numberOfImageDownloadAttemps+=1
        print 'saving file to '+dstfilename
        response=requests.get(downloadFilePath, stream=True)
        #print response.status_code
        if response.status_code != 200:
            print "Error status code is not 200="+str(response.status_code)+"downloadFilePath:"+downloadFilePath
            responseFailed+=1
            return False
        if response.status_code == 200:
            totalNumberOfImagesWeHave+=1
            #check directory exists before trying to write file and if not then make it
            if not os.path.exists(directory):
                os.makedirs(directory)

            with open(dstfilename, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
   
    if totalNumberOfImagesWeHave%10000==0 :
        print "totalNumber of images we have="+str(totalNumberOfImagesWeHave)

    return True

if __name__ == "__main__":
    main(sys.argv[1:])
